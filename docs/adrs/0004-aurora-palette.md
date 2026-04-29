# ADR-0004: Aurora brand palette

**Date:** 2026-04-22
**Status:** Accepted

## Context

After exploring multiple palette directions (warm editorial, jewel tones, copper luxury), we validated an Aurora palette with user preference research. Goal: a palette that reads as "energetic & approachable" in a Linear/Figma visual register.

## Decision

The **Aurora palette** is locked:

- **Canvas:** Deep Violet Black `#0A0612`
- **Surface:** Cosmic Surface `#1A0F2E`
- **Signature:** Electric Purple `#9C5BFF`
- **Accent:** Electric Pink `#EC4899`
- **Warm:** Solar Amber `#F59E0B`
- **Cool:** Sapphire Blue `#3B82F6`
- **Text:** Moonlight `#F5F3FF`
- **Success:** Emerald `#10B981` (status only)

**Signature gradient:** `linear-gradient(135deg, #9C5BFF 0%, #EC4899 50%, #F59E0B 100%)`

**Typography:** Inter (primary) + Instrument Serif Italic (gradient accents)

## Consequences

### Enables
- Distinctive identity in a sea of flat blue/green palettes
- Linear/Vercel/Raycast energy — modern tech signal
- Aurora gradient on headlines = instant brand recognition
- Dark-by-default approachable on mobile

### Costs
- Dark theme requires extra accessibility work (contrast ratios)
- Aurora gradient on small text reduces readability
- One signature gradient limits visual variety

### Mitigations
- Tokens in `/design/tokens/colors.json` — single source of truth
- Generated into all platforms
- WCAG 2.1 AA enforced via automated snapshot tests
