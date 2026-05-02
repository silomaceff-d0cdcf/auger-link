# AugerLink Typography

**Posture**: Readable in low-light + outdoor + glare conditions. Humanist sans-serif primary; monospace for hashes and identifiers; optional warm serif accent for hero/welcome screens only. NO display-weight web fonts; NO "tech-clean" geometric sans (DIN, Eurostile, Univers). The brand is rooted in farm tools + dusk light, not Bauhaus minimalism.

---

## Family Selection

### Primary: **Inter** (humanist sans-serif)

- Open-source (SIL OFL), maintained, multi-weight, multi-script
- Designed for screen readability at small sizes (Inter's UI optimization is documented)
- Wide hinting + low-contrast strokes work well at glare-heavy outdoor brightness
- Variable font available for fine weight control without bundling 6+ static files

**Weights bundled**:
- Inter Regular (400) — body text
- Inter Medium (500) — labels, button text
- Inter SemiBold (600) — section headings, navigation labels
- Inter Bold (700) — primary headlines

### Monospace: **JetBrains Mono** (for hashes, identifiers, code)

- Open-source (SIL OFL)
- Excellent character disambiguation (`0` vs `O`, `1` vs `l` vs `I`) — critical for LXMF dest hashes (32 hex chars; misreads matter)
- Consistent character width simplifies QR-paired hash display

**Weights bundled**:
- JetBrains Mono Regular (400) — hash display, code blocks
- JetBrains Mono Medium (500) — emphasis in monospaced text

### Optional accent: **Eczar** or **Source Serif** (for welcome screen + Phase G/H milestone moments)

- Warm humanist serif; matches concept art's emotional register
- Used SPARINGLY — welcome onboarding header, splash screen tagline. NOT body text.
- Bundled if Phase 6 (identity polish) integrates these moments

---

## Type Scale

| Style | Family | Weight | Size | Line Height | Letter Spacing | Use |
|---|---|---|---|---|---|---|
| `display-large` | Inter | 700 | 32sp | 1.2 | -0.5 | Welcome screen, splash |
| `display-medium` | Inter | 700 | 24sp | 1.25 | -0.25 | Screen titles |
| `headline-large` | Inter | 600 | 20sp | 1.3 | 0 | Section headings |
| `headline-medium` | Inter | 600 | 18sp | 1.35 | 0 | Card headers |
| `title-large` | Inter | 500 | 16sp | 1.4 | 0.15 | List item primary text |
| `body-large` | Inter | 400 | 16sp | 1.5 | 0.5 | Message body text |
| `body-medium` | Inter | 400 | 14sp | 1.45 | 0.25 | Subtitles, list secondary |
| `label-large` | Inter | 500 | 14sp | 1.4 | 0.5 | Buttons, tabs |
| `label-medium` | Inter | 500 | 12sp | 1.35 | 0.5 | Captions |
| `monospace-medium` | JetBrains Mono | 400 | 13sp | 1.5 | 0 | Hashes, hex output |
| `monospace-small` | JetBrains Mono | 400 | 11sp | 1.4 | 0 | Inline hash refs |

---

## Compose Mapping (planned)

```kotlin
val AugerLinkTypography = Typography(
    displayLarge   = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold,    fontSize = 32.sp, lineHeight = 38.sp),
    displayMedium  = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold,    fontSize = 24.sp, lineHeight = 30.sp),
    headlineLarge  = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold,fontSize = 20.sp, lineHeight = 26.sp),
    headlineMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold,fontSize = 18.sp, lineHeight = 24.sp),
    titleLarge     = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium,  fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge      = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal,  fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal,  fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge     = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium,  fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium    = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium,  fontSize = 12.sp, lineHeight = 16.sp),
)
```

Monospace styles applied per-component via `style = TextStyle(fontFamily = JetBrainsMono, ...)` on the specific Text composable, since Material3 Typography doesn't have a built-in monospace slot.

---

## Hash-Display Convention

LXMF destination hashes are 32 hex chars. Always display in **JetBrains Mono Medium** at `monospace-medium` (13sp) with grouping:

```
3fc4 df76 4746 c011 b54d 5dff 85d5 c08e
```

Four-char groups separated by hair-spaces (or ` `) — preserves selectability for paste while improving scannability. Tap a hash → copy full 32-char form to clipboard (no spaces).

---

## Accessibility Notes

- All body text meets WCAG AA contrast against the palette (see `palette.md` verification matrix)
- Material3's dynamic font scaling honored — user's system font-size preference scales the type scale
- Line heights generous (1.4-1.5x) for low-light + glare reading
- Letter spacing tight on display, slightly loose on labels (tabular feel) — matches the practical-tool emotional register

---

## Anti-patterns

- ❌ Display-weight tech-cool fonts (Eurostile, DIN, Aileron) — clash with the dusk-warmth brand
- ❌ Monospace for body text — feels like a terminal, not a messaging app
- ❌ Letter spacing ≥ 1px on body text — tracking-spread "tech sans" look
- ❌ All-caps section headings — corporate; doesn't match practical-tool register
- ❌ Pure black `#000000` text on cream — see palette.md (use `barn-dark` ratios; never invert to dark-on-light)
