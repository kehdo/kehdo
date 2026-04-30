# ADR-0006: AI personalization & model roadmap

**Date:** 2026-05-01
**Status:** Accepted

## Context

kehdo's reply quality depends on two AI capabilities: reading the chat from a screenshot, and generating reply suggestions in the user's voice. Three forces shape this ADR:

1. **Cost.** We're pre-revenue and budget-constrained. The expensive flagship-tier models break the budget at modest scale.
2. **Personalization without privacy compromise.** The product gets meaningfully better when it knows how the user writes — but the public commitment ("no training on user data") is load-bearing for the brand and rules out raw-conversation retention.
3. **Migration optionality.** We don't want to be locked to any vendor. The architecture has to allow swapping the model — eventually to one we own — without users feeling a regression.

Two strategy docs from prior work informed this ADR: a voice-fingerprint personalization plan and a contact-intelligence plan. Both assumed Gemini; this ADR aligns the assumption with the rest of the repo and resolves several mismatches surfaced during the audit.

## Decision

### Phase 1 stack (locked)

- **Primary LLM:** Vertex AI Gemini 2.0 Flash (not the AI Studio consumer API — Vertex AI is contractually opted out of training on our data, AI Studio's free tier is not).
- **Failover LLM:** OpenAI gpt-4o-mini, behind Resilience4j circuit breaker. Triggers only when Vertex AI fails. Tiny incremental cost, big resilience benefit.
- **OCR:** Google Cloud Vision API. Two-stage pipeline (OCR first, then LLM) — keeps the existing `:ai/ocr/` and `:ai/llm/` module split intact.

The landing page does NOT name the LLM provider. This is intentional — we keep vendor flexibility and avoid the kind of public commitment that would make a future swap a brand event.

### Five-phase migration roadmap

| Phase | What | Stack | Window |
|---|---|---|---|
| 1 | Hosted APIs do everything (MVP) | Vertex AI Gemini Flash + Google Vision | Month 0–3 |
| 2 | Build the flywheel — collect signals, fingerprints, synthetic data; no model change | Same as Phase 1 | Month 3–8 |
| 3 | Fine-tune open model (Qwen 2.5 7B and/or Llama 3.2 8B); A/B vs Gemini | Hosted + own | Month 8–14 |
| 4 | Own LLM for text generation; Vertex AI Gemini Vision still for OCR | Own LLM + Gemini Vision | Month 14–20 |
| 5 | Full independence — own vision model (LLaVA-Next or InternVL2) | 100% own | Month 20+ |

**Exit conditions** (gate the move forward, not time-based):

- Phase 1 → 2: app live, 1000+ MAU, behavioral logging confirmed working
- Phase 2 → 3: 10,000+ interaction signals collected, synthetic dataset ≥ 50,000 examples
- Phase 3 → 4: own model wins quality A/B ≥ 80% of the time vs. Vertex AI Gemini
- Phase 4 → 5: own vision model accuracy ≥ 92% on real chat screenshots across all supported apps

### Personalization architecture — three-layer prompt injection

Personalization is **prompt-based and model-agnostic.** The user's preferences are stored in PostgreSQL, not inside any model's context. Migrating to a new model = pointing at a new endpoint and continuing to inject the same fingerprint. Cold-start solved.

| Layer | Source | When injected |
|---|---|---|
| 1. App context | Visual app detection from screenshot | Always |
| 2. Global voice fingerprint | Aggregated behavioral signals (length pref, tone distribution, emoji affinity, formality, rejection patterns, option preference, app context) | When global confidence ≥ 0.3 (~10+ interactions) |
| 3. Contact-specific profile | Per-contact tone profile (Level 2 consent only) | When contact confidence ≥ 0.3 (~5+ interactions with this contact) |

**Layer 3 overrides Layer 2 when they conflict.** "Arti always gets warm casual" beats "this user trends formal."

### Three-level consent model

| Level | App detection | Contact name | Tone profile | People tab |
|---|---|---|---|---|
| 0 (no consent) | Auto from screenshot, **not stored against user** | Never stored | Never built | Hidden |
| 1 (app only) | Stored per session, resets each session | Never stored | Never built | Hidden |
| 2 (full) | Stored | Stored (name only) | Built over time | Visible |

**Default consent posture: ALL toggles default OFF.** Maximum privacy posture; users opt in deliberately. The consent screen design (currently mocked with "app detection ON") must be updated before this ships.

### App detection — visual only

**Method 1: visual detection from screenshot.** Always available, no Android permission required. Uses the LLM to identify the app from color scheme, layout, and UI chrome.

**Method 2 (PACKAGE_USAGE_STATS): explicitly excluded.** Three reasons:
- Google Play restricts this Special Access permission to a narrow whitelist (security/AV, parental controls, device admin). High rejection risk.
- Reading foreground package names is device-level inspection — contradicts the public privacy stance ("we don't fingerprint your device").
- Visual detection is ~94% accurate per the strategy doc; the 6% gap isn't worth the friction.

### Mode + tone unification

The strategy docs introduced "modes" (4 categories). The schema already has 12 "tones" with 6 more planned. We **unify these into a single hierarchy:**

```
modes (4):  CASUAL · FLIRTY · PROFESSIONAL · SINCERE
tones (18): each tone belongs to exactly one mode
```

UX: user picks a **mode** (4 chunky buttons, low cognitive load) → tones expand within. Power users on Pro tier get all 18; Starter users get a subset (one tone per mode). The existing `tones.is_pro` column already supports this — no schema change for the gating logic itself, just rows.

The 6 new tone codes (to grow from 12 → 18) are a copy/UX decision deferred to a separate PR.

### Privacy invariants (codified)

These are enforceable rules now mirrored in [backend/CLAUDE.md](../../backend/CLAUDE.md) AI pipeline rules 8–12:

1. **Header-only OCR** for contact name extraction. Never read message bodies during name extraction.
2. **Phone numbers never stored** in `contact_profiles.contact_name`.
3. **Layer 3 > Layer 2** when they conflict.
4. **Every reply emits an `interaction_signal` row.** Non-negotiable — this is the flywheel input.
5. **Fine-tuning corpora are restricted.** Synthetic data + behavioral signals + (with explicit Level-2+ consent) reply text the user selected. Raw conversation text and OCR'd message bodies are never eligible.

### Cost projection (Phase 1, Vertex AI Gemini 2.0 Flash)

Order-of-magnitude estimates as of 2026-05-01; verify pricing before committing.

| Users | Replies/mo | Vertex AI Gemini | + gpt-4o-mini failover | + Google Vision | **Total/mo** |
|---|---|---|---|---|---|
| 100 | ~15K | ~$3 | ~$0.15 | ~$23 | **~$26** |
| 1,000 | ~150K | ~$35 | ~$1.50 | ~$225 | **~$262** |
| 10,000 | ~1.5M | ~$350 | ~$15 | ~$2,250 | **~$2,615** |

**OCR dominates at scale.** When Phase-1 spend becomes painful, the lever is OCR (e.g., Gemini multimodal, request batching), not the LLM.

### Tables to add (V2 Flyway migration — separate PR)

All seven new tables ship in a single migration `V2__ai_personalization.sql`:

- `voice_fingerprints` — per-user aggregated behavioral profile
- `interaction_signals` — per-reply log: option shown/chosen, regen count, mode, app hint
- `synthetic_training_data` — synthetic chat scenarios for future fine-tuning
- `model_ab_tests` — per-user model assignment + quality scoring
- `user_consent` — three boolean toggles (app_detect, contact_name, people_tab)
- `contact_profiles` — per-contact tone profile (JSONB), with `is_excluded` and `deleted_at`
- `contact_interaction_signals` — per-contact reply selection log

### Cold-start migration (Phase 3 → Phase 4)

When the fine-tuned model goes live:

1. **Week 1: 5% canary.** Voice fingerprints + contact profiles inject into the new model's prompt. Monitor regeneration rate, copy rate, favorite rate.
2. **Week 2: 25% with shadow mode.** Vertex AI generates in shadow but isn't shown — used to compare quality silently.
3. **Week 3: 100% if shadow quality ≥ 90% match.** Vertex AI removed from text path.

Per-row rollback is supported via the existing `replies.model_used` column.

## Consequences

### Enables

- Privacy-preserving personalization that survives model migrations
- Cost-effective MVP launch (~$26/month at 100 users on Vertex AI Gemini Flash)
- A/B comparison substrate for future model swaps via existing `replies.model_used`
- Pro-tier upgrade path — Opus/own-model on Pro, cheaper model on Starter
- Unblocks Contact Intelligence as a feature without re-architecting

### Costs

- **Single-vendor concentration:** Google now provides LLM (Vertex AI) + OCR (Cloud Vision) + auth (Google Sign-In) + workspace (Sheets waitlist). Failover to gpt-4o-mini partially mitigates LLM risk; OCR has no failover yet.
- **Vertex AI setup overhead:** GCP project, IAM, billing — modest one-time cost.
- **Privacy commitments expand.** Contact names become a new stored data category. Privacy page and SECURITY.md must be updated **before** Contact Intelligence ships, not after.
- **Consent screen mock is stale.** Strategy doc shows app-detection ON by default; this ADR locks all defaults to OFF. Consent screen UI needs redesign before launch.
- **6 new tones** (12 → 18) are a deferred copy decision.

### Mitigations

- gpt-4o-mini wired as LLM circuit-breaker fallback (already mandatory per AI rule 4)
- All AI vendor names absent from the landing page — vendor swaps are not brand events
- The migration architecture (fingerprint in PostgreSQL, model-agnostic prompt injection) is itself a mitigation — the cost of a vendor swap is bounded
- Existing `replies.model_used` enables per-row rollback during canary releases

## Open work / out of scope for this ADR

- `V2__ai_personalization.sql` Flyway migration (separate PR)
- OpenAPI spec updates for consent endpoints, contact CRUD, interaction-signal POST (separate PR)
- Frontend consent screens — onboarding global consent + per-contact prompt (separate PR; design refresh required)
- Subscription gating — which 4 tones are Starter, which 18 are Pro (copy + product decision)
- The 6 new tone codes (12 → 18) — copy decision
- `application.yml` rewire from Anthropic/OpenAI to Vertex AI + gpt-4o-mini (separate backend PR, follows feature-branch flow)

## References

- Voice fingerprint & model roadmap strategy doc (shared 2026-05-01)
- Contact Intelligence strategy doc (shared 2026-05-01)
- [SECURITY.md](../../SECURITY.md) — privacy commitments
- [web/src/app/privacy/page.tsx](../../web/src/app/privacy/page.tsx) — public privacy policy
- [V1__init_schema.sql](../../backend/app/src/main/resources/db/migration/V1__init_schema.sql) — existing behavioral signal columns (`replies.is_favorited`, `is_copied`, `tone_code`, `model_used`)
