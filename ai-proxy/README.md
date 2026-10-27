# Job Tracker AI proxy (Cloudflare Worker)

Holds the Gemini API key server-side so it is never in the APK. The app sends structured fields
(`tool` = `parse` | `cover` | `followup`), the Worker builds the prompt, rate-limits per device and
per IP, caps input size, and falls back across Gemini models.

## Deploy (once)

Needs a free Cloudflare account (https://dash.cloudflare.com/sign-up) and Node 18+.

```bash
cd ai-proxy
npm install
npx wrangler login                       # opens the browser, approve access
npx wrangler secret put GEMINI_API_KEY   # paste a NEW key from https://aistudio.google.com/apikey
npx wrangler deploy                      # prints https://job-tracker-ai.<your-subdomain>.workers.dev
```

Then put that URL in `gradle.properties`:

```
aiProxyUrl=https://job-tracker-ai.<your-subdomain>.workers.dev
```

and rebuild the app. Check it works: open `<url>/health` in a browser, it returns `{"ok":true}`.

## Notes

- Use a dedicated Gemini key for this Worker (not the one that was in earlier builds). Rotate it from AI Studio if abused.
- Free Gemini quota is shared by all users. When it runs out the app shows a friendly message and offers
  the "use my own key" option.
- Cloudflare free plan: 100,000 Worker requests/day. Rate limits are per Cloudflare location and best-effort.
- Nothing is logged by this code. Do not enable request-body logging in the Cloudflare dashboard.
- Hardening later: add Play Integrity attestation so only genuine installs of the app can call the Worker.
- Tests: `npm test`
