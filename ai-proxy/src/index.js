// Cloudflare Worker: server-side proxy for Google Gemini.
// The Gemini key lives only in the GEMINI_API_KEY secret. The app sends structured fields,
// never raw prompts, so the endpoint cannot be used as a general-purpose chatbot.

// Fastest/cheapest first: the app needs short answers quickly, and flash-lite has the most free quota.
const MODELS = [
  "gemini-flash-lite-latest",
  "gemini-2.5-flash-lite",
  "gemini-flash-latest",
  "gemini-2.5-flash",
];

const HEDGE_MS = 6_000;
const MAX_PARALLEL = 2;
const TOTAL_BUDGET_MS = 40_000;

const MAX_BODY_BYTES = 20_000;
const MAX_OUTPUT_TOKENS = 1500;
const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

const FIELD_LIMITS = {
  rawText: 8000,
  companyName: 200,
  jobTitle: 200,
  notes: 6000,
  candidateHighlights: 2000,
  status: 50,
  emailType: 100,
};

function clean(fields, name) {
  const v = fields?.[name];
  return typeof v === "string" ? v.slice(0, FIELD_LIMITS[name]).trim() : "";
}

const TOOLS = {
  parse(f) {
    const rawText = clean(f, "rawText");
    if (!rawText) return null;
    return `
You are an expert ATS (Applicant Tracking System) parser. Extract structured job application details from the following job posting text.
Output MUST BE ONLY a raw valid JSON object without markdown fences or extra explanations.
JSON schema:
{
  "companyName": "Exact Company Name",
  "jobTitle": "Job Title / Role",
  "salary": "Compensation or Salary Range if specified (otherwise empty string)",
  "keyPoints": "Concise 2-3 bullet summary of top qualifications or highlights",
  "detectedStatus": "Applied"
}

Job Posting Text:
${rawText}`.trim();
  },

  cover(f) {
    const companyName = clean(f, "companyName");
    const jobTitle = clean(f, "jobTitle");
    if (!companyName || !jobTitle) return null;
    const highlights =
      clean(f, "candidateHighlights") ||
      "Experienced software engineer passionate about modern product development, clean architecture, and high performance systems.";
    return `
Write a compelling, professional, modern, and concise tailored cover letter and key interview talking points for the following job:
- Company: ${companyName}
- Role / Job Title: ${jobTitle}
- Context / Job Details: ${clean(f, "notes")}
- Candidate Background Highlights: ${highlights}

Format output cleanly with:
1. Tailored Cover Letter (3 short impactful paragraphs)
2. 3 Key Talking Points for Why I'm a Great Match`.trim();
  },

  followup(f) {
    const companyName = clean(f, "companyName");
    const jobTitle = clean(f, "jobTitle");
    if (!companyName || !jobTitle) return null;
    return `
Draft a polished, professional email for the following job application stage:
- Email Scenario / Intent: ${clean(f, "emailType") || "Post-Interview Thank You"}
- Company: ${companyName}
- Role: ${jobTitle}
- Current Application Status: ${clean(f, "status")}
- Application Notes / Context: ${clean(f, "notes")}

Provide:
- Subject Line
- Professional Email Body (polite, confident, expressing clear value and gratitude)`.trim();
  },
};

function json(body, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "content-type": "application/json; charset=utf-8", "cache-control": "no-store" },
  });
}

const fail = (code, status) => json({ error: { code } }, status);

// Reasoning ("thinking") adds many seconds and eats the output-token budget; these tasks do not need it.
function requestBody(prompt, withThinkingOff) {
  const generationConfig = { maxOutputTokens: MAX_OUTPUT_TOKENS, temperature: 0.7 };
  if (withThinkingOff) generationConfig.thinkingConfig = { thinkingBudget: 0 };
  return JSON.stringify({ contents: [{ parts: [{ text: prompt }] }], generationConfig });
}

// Free-tier latency is uneven, so if a model has not answered after a few seconds a second model is
// started in parallel and the first good answer wins (the loser is cancelled).
async function callGemini(prompt, apiKey, hedgeMs = HEDGE_MS) {
  const controller = new AbortController();
  const signal = AbortSignal.any([controller.signal, AbortSignal.timeout(TOTAL_BUDGET_MS)]);
  let sawQuota = false;

  const attempt = async (model) => {
    const send = (thinkingOff) =>
      fetch(`https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`, {
        method: "POST",
        headers: { "content-type": "application/json", "x-goog-api-key": apiKey },
        body: requestBody(prompt, thinkingOff),
        signal,
      });
    let res = await send(true);
    // A model that rejects the thinking setting gets one retry without it.
    if (res.status === 400) res = await send(false);
    if (res.ok) {
      const data = await res.json().catch(() => null);
      const text = data?.candidates?.[0]?.content?.parts?.map((p) => p.text ?? "").join("") ?? "";
      if (text.trim()) return { text, model };
      throw new Error("empty");
    }
    if (res.status === 429) sawQuota = true;
    if (![404, 429, 500, 502, 503, 504].includes(res.status)) throw Object.assign(new Error("hard"), { hard: true });
    throw new Error(String(res.status));
  };

  return new Promise((resolve) => {
    let next = 0;
    let pending = 0;
    let done = false;
    const finish = (result) => {
      if (done) return;
      done = true;
      controller.abort();
      resolve(result);
    };
    const launch = () => {
      if (done || pending >= MAX_PARALLEL) return;
      if (next >= MODELS.length) {
        if (pending === 0) finish({ error: sawQuota ? "quota" : "upstream" });
        return;
      }
      const model = MODELS[next++];
      pending++;
      const hedge = setTimeout(launch, hedgeMs);
      attempt(model).then(
        (result) => {
          clearTimeout(hedge);
          finish(result);
        },
        (err) => {
          clearTimeout(hedge);
          pending--;
          if (err?.hard) finish({ error: "upstream" });
          else launch();
        },
      );
    };
    launch();
  });
}

export default {
  async fetch(request, env) {
    const url = new URL(request.url);

    if (request.method === "GET" && url.pathname === "/health") return json({ ok: true });
    if (request.method !== "POST" || url.pathname !== "/v1/ai") return fail("not_found", 404);
    if (!env.GEMINI_API_KEY) return fail("not_configured", 503);

    const deviceId = request.headers.get("x-device-id") ?? "";
    if (!UUID_RE.test(deviceId)) return fail("bad_device", 400);

    const declared = Number(request.headers.get("content-length") ?? 0);
    if (declared > MAX_BODY_BYTES) return fail("too_large", 413);

    const ip = request.headers.get("cf-connecting-ip") ?? "unknown";
    const [byDevice, byIp] = await Promise.all([
      env.DEVICE_LIMITER.limit({ key: `d:${deviceId}` }),
      env.IP_LIMITER.limit({ key: `i:${ip}` }),
    ]);
    if (!byDevice.success || !byIp.success) return fail("rate_limited", 429);

    const raw = await request.text();
    if (raw.length > MAX_BODY_BYTES) return fail("too_large", 413);

    let body;
    try {
      body = JSON.parse(raw);
    } catch {
      return fail("bad_json", 400);
    }

    const build = typeof body?.tool === "string" && Object.hasOwn(TOOLS, body.tool) ? TOOLS[body.tool] : null;
    if (!build) return fail("bad_tool", 400);
    const prompt = build(body.fields);
    if (!prompt) return fail("bad_fields", 400);

    const result = await callGemini(prompt, env.GEMINI_API_KEY, Number(env.HEDGE_MS) || undefined);
    if (result.error === "quota") return fail("quota", 429);
    if (result.error) return fail("upstream", 502);
    const response = json({ text: result.text });
    response.headers.set("x-model", result.model);
    return response;
  },
};
