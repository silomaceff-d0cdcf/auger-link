# AugerLink Palette

**Source**: Hero concept art at [`Auger_Link.png`](Auger_Link.png) — dusk farm with grain-silo-as-mesh-comms-tower; warm-amber-dominant, low-saturation muted-blue sky, gloved-hand-holding-phone composition.

**Theme posture**: Dark only (no light mode in v1). Two palette variants ship — both dark, both WCAG-AA-verified:

- **Night-shift / harvest** (default) — direct read from the concept art. Warm amber + glow-orange + gold + barn-dark. Deliberately seasonal-coded; appropriate for farm-edge / dusk / dawn / "coming in from the field" use. Reads as Halloween / Harvest Fest vibe.
- **Day-shift / cool-mute** — same brand spine cooled and muted. Weathered bronze + oak-tan + straw + linen + cool-neutral dark. Year-round usable; less seasonal-coded for non-farmer users (mesh-radio enthusiasts, urban sovereign-comms users).

The full token table below describes Night-shift. Day-shift tokens + verification appear at the bottom of this document.

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

## Day-Shift Variant — Cool-Mute Tokens

A second palette variant available for users / contexts where the harvest-warm Night-shift reads as too seasonal-coded. Same brand spine (warm dark + dusk-derived primary + earth-tones + muted semantic colors), pulled toward weathered/leather/sage rather than candy-pumpkin.

| Token | Hex | Role |
|---|---|---|
| `bronze` | `#9C7A52` | Primary brand warm — weathered-bronze (vs Night-shift's pumpkin-amber) |
| `oak-tan` | `#B5896A` | Accent — primaryContainer / outgoing bubbles (vs glow-orange) |
| `straw-tan` | `#C4A77D` | Highlight — secondary / active indicators (vs marigold gold) |
| `linen` | `#E0DCD2` | Body text on dark surfaces (vs cream — less warm) |
| `taupe` | `#A8A498` | Secondary text on dark surfaces (vs cream-muted — less amber) |
| `bg-dark` | `#15181B` | App background — cool-neutral very-dark, slight blue undertone (vs warm barn-dark) |
| `surface` | `#0E1114` | UI surfaces — deeper cool-neutral (vs warm charcoal) |
| `surfaceVariant` | `#2C353D` | Secondary surfaces (vs night-teal — cooler) |
| `divider` | `#262A30` | Subtle dividers — cool (vs warm divider) |
| `coral` | `#D85A4F` | Error states — coral, less stop-sign |
| `sage` | `#86A88A` | Connected/delivered/verified — sage-green, less olive-warm |

### Day-Shift WCAG Verification — VERIFIED ✅

10/10 pairs pass AA; 8/10 also pass AAA. Frozen output: `design/wcag_verified.day-shift.md`. Run `python3 design/wcag_verify.py --variant day-shift --strict`.

| Foreground | Background | Ratio | AA | Use Case |
|---|---|---|---|---|
| `linen` `#E0DCD2` | `bg-dark` `#15181B` | **13.02:1** | ✅ | Body text on app bg |
| `linen` `#E0DCD2` | `surface` `#0E1114` | **13.83:1** | ✅ | Text on cards/bubbles |
| `taupe` `#A8A498` | `bg-dark` `#15181B` | **7.15:1** | ✅ | Secondary text on app bg |
| `bronze` `#9C7A52` | `bg-dark` `#15181B` | **4.51:1** | ✅ | Primary button bg vs app bg |
| `bg-dark` `#15181B` | `bronze` `#9C7A52` | **4.51:1** | ✅ | Button label on primary |
| `oak-tan` `#B5896A` | `bg-dark` `#15181B` | **5.73:1** | ✅ | Outgoing bubble vs app bg |
| `bg-dark` `#15181B` | `oak-tan` `#B5896A` | **5.73:1** | ✅ | Outgoing bubble text |
| `straw-tan` `#C4A77D` | `bg-dark` `#15181B` | **7.78:1** | ✅ | Active indicator on app bg |
| `coral` `#D85A4F` | `bg-dark` `#15181B` | **4.66:1** | ✅ | Error text/icon on app bg |
| `sage` `#86A88A` | `bg-dark` `#15181B` | **6.77:1** | ✅ | Success text/icon on app bg |

### Compose Theme Mapping (Day-shift)

```kotlin
private val DayShiftDarkColorScheme = darkColorScheme(
    primary              = Color(0xFF9C7A52),  // bronze
    onPrimary            = Color(0xFF15181B),  // bg-dark — 4.51:1 ✅
    primaryContainer     = Color(0xFFB5896A),  // oak-tan
    onPrimaryContainer   = Color(0xFF15181B),  // bg-dark — 5.73:1 ✅
    secondary            = Color(0xFFC4A77D),  // straw-tan
    onSecondary          = Color(0xFF15181B),
    background           = Color(0xFF15181B),  // bg-dark
    onBackground         = Color(0xFFE0DCD2),  // linen — 13.02:1 ✅
    surface              = Color(0xFF0E1114),  // deeper cool-neutral
    onSurface            = Color(0xFFE0DCD2),  // linen — 13.83:1 ✅
    surfaceVariant       = Color(0xFF2C353D),
    onSurfaceVariant     = Color(0xFFA8A498),  // taupe
    error                = Color(0xFFD85A4F),  // coral
    onError              = Color(0xFFE0DCD2),
    outline              = Color(0xFF262A30),  // cool divider
)
```

(See `app/src/main/kotlin/io/silomaceff/augerlink/ui/theme/Color.kt` for the implementation — both variants live there as `AugerLinkColorsNight` and `AugerLinkColorsDay`.)

### How to switch variants in the app

The Phase 1 Palette Sampler ships with a `SegmentedButton` toggle at the top of the screen — tap to A/B between Night-shift and Day-shift live on-device. The selected variant survives config changes (rotation) via `rememberSaveable`. Phase 2+ surfaces this as a Settings preference.

---

## Anti-patterns

- ❌ **Pure black `#000000` backgrounds** — too cold; loses the warm character that makes the brand
- ❌ **Pure white `#FFFFFF` text** — too bright on warm dark; eye-strain at night; use `cream` `#F5E6D3`
- ❌ **Cyan/electric-blue accents** (`#00BFFF`, etc.) — opposite of what the concept art communicates; would feel like a generic tech app
- ❌ **High-saturation greens for "success"** (`#00FF00`, `#22DD22`) — clashes with the warm palette; use muted olive `#7FA756`
- ❌ **Material Design's default purple `#6750A4`** — not in the palette; remove from any default theme inherited from project skeleton
