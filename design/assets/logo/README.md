# kehdo logo assets

Single source of truth for the kehdo brand mark and lockups.

## Files

- [`kehdo-mark.svg`](kehdo-mark.svg) — the icon mark only. 40×44 viewBox.
  Aurora gradient (`#9C5BFF` → `#EC4899` → `#F59E0B`) on the strokes;
  base bar is the same gradient at 0.4 opacity.

## Lockup variants

The mark composes with the wordmark and tagline in three combinations:

| Variant | When to use |
|---|---|
| **Mark only** | Tight UI corners (Android app icon, favicon, share-sheet thumbnail), busy hero compositions where text would compete |
| **Mark + wordmark** | Default usage — Nav, Footer, app splash. Wordmark "kehdo" sits to the right of the mark, baseline-aligned, in Jost font |
| **Mark + wordmark + tagline** | OG/social images, marketing collateral. Tagline "JUST · SAY · IT" sits beneath the wordmark in uppercase, letter-spaced, ~50% the wordmark's font-size |

## Typography lock

| Element | Font |
|---|---|
| Wordmark "kehdo" | **Jost** (Google Fonts), weight 600 |
| Tagline "JUST · SAY · IT" | **Jost**, weight 500, uppercase, letter-spacing wide, dot separator (`·`) |
| Body / UI elsewhere | Inter (already locked) |
| Hero gradient accents | Instrument Serif Italic (already locked, e.g., the word "confidence" in "Reply with quiet confidence.") |

## Color rules

- **Always use the Aurora gradient on the mark.** Never apply a flat
  color unless the surface explicitly demands monochrome (e.g., 1-bit
  print, watermark on photography).
- The wordmark uses `text-moonlight` (`#F5F3FF`) on dark surfaces or
  `text-bg` (`#0A0612`) on light. Don't apply the gradient to the
  wordmark — keep the gradient anchored to the mark only, so the mark
  carries the brand color and the wordmark stays legible.

## Don't

- Don't recolor the gradient stops; they're locked
  ([`/design/tokens/colors.json`](../../tokens/colors.json), key
  `gradAurora`)
- Don't change the mark's stroke proportions, the base-bar opacity, or
  the relative spacing between mark and wordmark
- Don't mix Jost with the Aurora gradient applied to the wordmark — one
  brand-color element at a time, mark or wordmark, never both
- Don't use the old text-only "kehdo" wordmark from before the logo
  shipped — replace any sighting with the proper lockup
