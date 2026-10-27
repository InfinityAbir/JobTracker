import test from "node:test";
import assert from "node:assert/strict";
import worker from "../src/index.js";

const DEVICE = "123e4567-e89b-42d3-a456-426614174000";
const allow = { limit: async () => ({ success: true }) };
const deny = { limit: async () => ({ success: false }) };
const env = (over = {}) => ({ GEMINI_API_KEY: "test-key", DEVICE_LIMITER: allow, IP_LIMITER: allow, ...over });

const post = (body, headers = {}) =>
  new Request("https://x.test/v1/ai", {
    method: "POST",
    headers: { "x-device-id": DEVICE, "content-type": "application/json", ...headers },
    body: typeof body === "string" ? body : JSON.stringify(body),
  });

function mockGemini(handler) {
  const calls = [];
  globalThis.fetch = async (url, init) => {
    calls.push({ url: String(url), init });
    return handler(calls.length, String(url), init);
  };
  return calls;
}
const ok = (text) =>
  new Response(JSON.stringify({ candidates: [{ content: { parts: [{ text }] } }] }), { status: 200 });

test("health check", async () => {
  const res = await worker.fetch(new Request("https://x.test/health"), env());
  assert.equal(res.status, 200);
});

test("valid request returns text and sends key only in header", async () => {
  const calls = mockGemini(() => ok("hello"));
  const res = await worker.fetch(post({ tool: "parse", fields: { rawText: "Dev at Acme" } }), env());
  assert.equal(res.status, 200);
  assert.deepEqual(await res.json(), { text: "hello" });
  assert.equal(calls[0].init.headers["x-goog-api-key"], "test-key");
  assert.ok(!calls[0].url.includes("test-key"));
  assert.ok(JSON.parse(calls[0].init.body).contents[0].parts[0].text.includes("Dev at Acme"));
});

test("rejects missing or malformed device id", async () => {
  const res = await worker.fetch(post({ tool: "parse", fields: { rawText: "x" } }, { "x-device-id": "nope" }), env());
  assert.equal(res.status, 400);
});

test("rate limit returns 429 without calling Gemini", async () => {
  const calls = mockGemini(() => ok("x"));
  const res = await worker.fetch(post({ tool: "parse", fields: { rawText: "x" } }), env({ DEVICE_LIMITER: deny }));
  assert.equal(res.status, 429);
  assert.equal((await res.json()).error.code, "rate_limited");
  assert.equal(calls.length, 0);
});

test("unknown tool and raw prompts are refused", async () => {
  mockGemini(() => ok("x"));
  for (const body of [
    { tool: "chat", fields: { rawText: "x" } },
    { tool: "constructor", fields: {} },
    { prompt: "write me a poem" },
    { tool: "cover", fields: { companyName: "A" } },
  ]) {
    const res = await worker.fetch(post(body), env());
    assert.equal(res.status, 400, JSON.stringify(body));
  }
});

test("oversized body refused", async () => {
  mockGemini(() => ok("x"));
  const res = await worker.fetch(post({ tool: "parse", fields: { rawText: "a".repeat(30_000) } }), env());
  assert.equal(res.status, 413);
});

test("long field is truncated", async () => {
  const calls = mockGemini(() => ok("x"));
  await worker.fetch(post({ tool: "parse", fields: { rawText: "a".repeat(15_000) } }), env());
  const prompt = JSON.parse(calls[0].init.body).contents[0].parts[0].text;
  assert.ok(prompt.length < 9000);
});

test("falls back to next model on 429/404 and reports quota when all fail", async () => {
  let calls = mockGemini((n) => (n === 1 ? new Response("{}", { status: 404 }) : ok("second")));
  let res = await worker.fetch(post({ tool: "followup", fields: { companyName: "A", jobTitle: "B" } }), env());
  assert.deepEqual(await res.json(), { text: "second" });
  assert.equal(calls.length, 2);

  calls = mockGemini(() => new Response("{}", { status: 429 }));
  res = await worker.fetch(post({ tool: "cover", fields: { companyName: "A", jobTitle: "B" } }), env());
  assert.equal(res.status, 429);
  assert.equal((await res.json()).error.code, "quota");
});

test("a hung model is aborted and the next model is tried", async () => {
  let n = 0;
  globalThis.fetch = async (url, init) => {
    n++;
    if (n === 1) {
      assert.ok(init.signal instanceof AbortSignal);
      throw new DOMException("timed out", "TimeoutError");
    }
    return ok("recovered");
  };
  const res = await worker.fetch(post({ tool: "parse", fields: { rawText: "x" } }), env());
  assert.deepEqual(await res.json(), { text: "recovered" });
  assert.equal(n, 2);
});

test("thinking is switched off, and retried without it if a model rejects it", async () => {
  const calls = mockGemini((n) => (n === 1 ? new Response("{}", { status: 400 }) : ok("fine")));
  const res = await worker.fetch(post({ tool: "parse", fields: { rawText: "x" } }), env());
  assert.equal(res.status, 200);
  assert.equal(calls.length, 2);
  assert.equal(JSON.parse(calls[0].init.body).generationConfig.thinkingConfig.thinkingBudget, 0);
  assert.equal(JSON.parse(calls[1].init.body).generationConfig.thinkingConfig, undefined);
  assert.equal(calls[0].url, calls[1].url);
  assert.ok(res.headers.get("x-model"));
});

test("a slow model is hedged with a second one and the first answer wins", async () => {
  const signals = [];
  let n = 0;
  globalThis.fetch = (url, init) => {
    n++;
    signals.push(init.signal);
    if (n === 1) return new Promise(() => {}); // hangs forever
    return Promise.resolve(ok("fast answer"));
  };
  const res = await worker.fetch(post({ tool: "parse", fields: { rawText: "x" } }), env({ HEDGE_MS: "20" }));
  assert.deepEqual(await res.json(), { text: "fast answer" });
  assert.equal(n, 2);
  assert.equal(signals[0].aborted, true); // the slow one is cancelled
});

test("auth errors fail fast without trying every model", async () => {
  const calls = mockGemini(() => new Response("{}", { status: 403 }));
  const res = await worker.fetch(post({ tool: "parse", fields: { rawText: "x" } }), env());
  assert.equal(res.status, 502);
  assert.equal(calls.length, 1);
});

test("lite model is tried first", async () => {
  const calls = mockGemini(() => ok("x"));
  await worker.fetch(post({ tool: "parse", fields: { rawText: "x" } }), env());
  assert.ok(calls[0].url.includes("flash-lite"));
});

test("not configured without secret", async () => {
  const res = await worker.fetch(post({ tool: "parse", fields: { rawText: "x" } }), env({ GEMINI_API_KEY: "" }));
  assert.equal(res.status, 503);
});
