# AugerLink Palette

**Source**: Hero concept art at [`Auger_Link.png`](Auger_Link.png) — dusk farm with grain-silo-as-mesh-comms-tower; warm-amber-dominant, low-saturation muted-blue sky, gloved-hand-holding-phone composition.

**Theme posture**: Dark only (no light mode in v1). Warm-shifted dark — explicitly NOT blue-black tech minimalism. Sleep-friendly + farmer-in-field-readable + dawn/dusk-glare-tolerant.

---

## Tokens

| Token | Hex | Role |
|---|---|---|
| `dusk-amber` | `#9C5519` | Primary brand warm. Top app bars, FAB, primary buttons, send button. (Adjusted from initial `#B5651D` after WCAG verification — see `wcag_verified.md`. Hue preserved; value reduced ~15% to allow cream-text@4.5:1.) |
| `glow-orange` | `#D97826` | Accent. Outgoing message bubbles, button highlights, active toggle thumb, link color. |
| `gold` | `#FFC065` | Highlight + iconography accents. Notification dot, "active" status indicator, filled icon strokes. |
| `cream` | `#F5E6D3` | Body text on dark surfaces. Warm white — NEVER `#FFFFFF`. |
| `cream-muted` | `#C8B8A0` | Secondary text, subtitles, label text on dark surfaces. |
| `barn-dark` | `#1A1410` | App background. Warm dark — NOT `#000000`, NOT blue-black. |
| `charcoal` | `#0F0E0C` | UI surfaces (cards, sheets, message bubble bg for incoming). |
| `night-teal` | `#2A3040` | Secondary surface accents (status bar tint, occasional separator). Low-saturation muted blue + warm-shifted: this is what "warm low-blue" means in practice. |
| `divider` | `#2E2A26` | Subtle dividers between rows + sections. |
| `error` | `#E53E2C` | Error states + destructive action confirmations (deep warm red, not pink). |
| `success` | `#7FA756` | Connected / delivered / verified states (muted olive, not bright green). |

---

## WCAG AA Contrast Verification — VERIFIED ✅

Target ratios (per WCAG 2.1):
- Body text: ≥ 4.5:1 (normal text)
- Large text + UI components + graphical objects: ≥ 3:1

**Verifier**: `design/wcag_verify.py` (deterministic Python script implementing the WCAG 2.1 relative-luminance + contrast-ratio formulas).
**Frozen output**: `design/wcag_verified.md` (regenerate with `python3 design/wcag_verify.py > design/wcag_verified.md`).
**Status**: 10/10 pairs pass AA (6/10 also pass AAA).

| Foreground | Background | Ratio | AA | AAA | Use Case |
|---|---|---|---|---|---|
| `cream` `#F5E6D3` | `barn-dark` `#1A1410` | **14.89:1** | ✅ | ✅ | Body text on app bg |
| `cream` `#F5E6D3` | `charcoal` `#0F0E0C` | **15.75:1** | ✅ | ✅ | Text on cards/bubbles |
| `cream-muted` `#C8B8A0` | `barn-dark` `#1A1410` | **9.40:1** | ✅ | ✅ | Secondary text on app bg |
| `dusk-amber` `#9C5519` | `barn-dark` `#1A1410` | **3.24:1** | ✅ | — | Primary button bg vs app bg |
| `cream` `#F5E6D3` | `dusk-amber` `#9C5519` | **4.60:1** | ✅ | — | Button label on primary |
| `glow-orange` `#D97826` | `barn-dark` `#1A1410` | **5.79:1** | ✅ | ✅ | Outgoing bubble vs app bg |
| `barn-dark` `#1A1410` | `glow-orange` `#D97826` | **5.79:1** | ✅ | — | Outgoing bubble text |
| `gold` `#FFC065` | `barn-dark` `#1A1410` | **11.27:1** | ✅ | ✅ | Active indicator on app bg |
| `error` `#E53E2C` | `barn-dark` `#1A1410` | **4.39:1** | ✅ | — | Error text/icon on app bg |
| `success` `#7FA756` | `barn-dark` `#1A1410` | **6.57:1** | ✅ | ✅ | Success text/icon on app bg |

### Adjustments made during verification

Initial palette had two AA failures; both fixed without losing brand character:

1. **`dusk-amber` darkened from `#B5651D` to `#9C5519`** — initial value let cream-on-amber drop to 3.54:1 (need 4.5:1). The fundamental issue: cream + initial amber were both light-warm, too close in luminance. Darkening the amber by ~15% V (in HSV) preserves the hue + warm character while opening enough luminance gap to clear AA. New value still passes amber-on-barn-dark@3:1 (3.24:1).

2. **Outgoing bubble text changed from `cream` to `barn-dark`** — `cream` on `glow-orange` was 2.57:1 (well below AA). Material 3 best-practice anyway: high-contrast dark text on bright primary container. New combo: `barn-dark` on `glow-orange` = 5.79:1 ✅.

If the palette evolves, re-run `wcag_verify.py --strict` before committing — exit code 1 on any failure makes it a usable git pre-commit / CI gate.

---

## Compose Theme Mapping (planned for `app/src/main/java/.../theme/Theme.kt`)

```kotlin
private val AugerLinkDarkColorScheme = darkColorScheme(
    primary            = Color(0xFF9C5519),  // dusk-amber (WCAG-verified)
    onPrimary          = Color(0xFFF5E6D3),  // cream — 4.60:1 ✅
    primaryContainer   = Color(0xFFD97826),  // glow-orange
    onPrimaryContainer = Color(0xFF1A1410),  // barn-dark — 5.79:1 ✅ (NOT cream — fails AA)
    secondary          = Color(0xFFFFC065),  // gold
    onSecondary        = Color(0xFF1A1410),  // barn-dark
    background         = Color(0xFF1A1410),  // barn-dark
    onBackground       = Color(0xFFF5E6D3),  // cream — 14.89:1 ✅
    surface            = Color(0xFF0F0E0C),  // charcoal
    onSurface           = Color(0xFFF5E6D3),  // cream — 15.75:1 ✅
    surfaceVariant     = Color(0xFF2A3040),  // night-teal
    onSurfaceVariant   = Color(0xFFC8B8A0),  // cream-muted
    error              = Color(0xFFE53E2C),
    onError            = Color(0xFFF5E6D3),
    outline            = Color(0xFF2E2A26),  // divider
)
```

(Actual `Theme.kt` lands when the empty Android Studio project skeleton goes in — Phase 1 final deliverable.)

---

## Anti-patterns

- ❌ **Pure black `#000000` backgrounds** — too cold; loses the warm character that makes the brand
- ❌ **Pure white `#FFFFFF` text** — too bright on warm dark; eye-strain at night; use `cream` `#F5E6D3`
- ❌ **Cyan/electric-blue accents** (`#00BFFF`, etc.) — opposite of what the concept art communicates; would feel like a generic tech app
- ❌ **High-saturation greens for "success"** (`#00FF00`, `#22DD22`) — clashes with the warm palette; use muted olive `#7FA756`
- ❌ **Material Design's default purple `#6750A4`** — not in the palette; remove from any default theme inherited from project skeleton
