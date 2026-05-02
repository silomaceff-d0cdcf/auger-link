# AugerLink Palette

**Source**: Hero concept art at [`Auger_Link.png`](Auger_Link.png) — dusk farm with grain-silo-as-mesh-comms-tower; warm-amber-dominant, low-saturation muted-blue sky, gloved-hand-holding-phone composition.

**Theme posture**: Dark only (no light mode in v1). Warm-shifted dark — explicitly NOT blue-black tech minimalism. Sleep-friendly + farmer-in-field-readable + dawn/dusk-glare-tolerant.

---

## Tokens

| Token | Hex | Role |
|---|---|---|
| `dusk-amber` | `#B5651D` | Primary brand warm. Top app bars, FAB, primary buttons, send button. Already in testbed Settings TopAppBar. |
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

## WCAG AA Contrast Verification (planned)

Target ratios:
- Body text on background: ≥ 4.5:1 (normal text)
- Large text on background: ≥ 3:1
- UI components + graphical objects: ≥ 3:1

**To verify** (Phase 1 follow-up — run a contrast checker):

| Foreground | Background | Min Ratio | Use Case |
|---|---|---|---|
| `cream` `#F5E6D3` | `barn-dark` `#1A1410` | 4.5:1 | Body text on app bg |
| `cream` `#F5E6D3` | `charcoal` `#0F0E0C` | 4.5:1 | Text on cards/bubbles |
| `cream-muted` `#C8B8A0` | `barn-dark` `#1A1410` | 4.5:1 | Secondary text on app bg |
| `dusk-amber` `#B5651D` | `barn-dark` `#1A1410` | 3:1 | Primary button bg vs app bg |
| `cream` `#F5E6D3` | `dusk-amber` `#B5651D` | 4.5:1 | Button label on primary |
| `glow-orange` `#D97826` | `barn-dark` `#1A1410` | 3:1 | Outgoing bubble vs app bg |
| `cream` `#F5E6D3` | `glow-orange` `#D97826` | 4.5:1 | Outgoing bubble text |
| `gold` `#FFC065` | `barn-dark` `#1A1410` | 3:1 | Active indicator on app bg |
| `error` `#E53E2C` | `barn-dark` `#1A1410` | 3:1 | Error text/icon on app bg |
| `success` `#7FA756` | `barn-dark` `#1A1410` | 3:1 | Success text/icon on app bg |

If any combo fails AA, **adjust the foreground** (slight value bump toward the brighter end) — keep the hue and the warm character.

---

## Compose Theme Mapping (planned for `app/src/main/java/.../theme/Theme.kt`)

```kotlin
private val AugerLinkDarkColorScheme = darkColorScheme(
    primary           = Color(0xFFB5651D),  // dusk-amber
    onPrimary         = Color(0xFFF5E6D3),  // cream
    primaryContainer  = Color(0xFFD97826),  // glow-orange
    onPrimaryContainer= Color(0xFFF5E6D3),
    secondary         = Color(0xFFFFC065),  // gold
    onSecondary       = Color(0xFF1A1410),  // barn-dark
    background        = Color(0xFF1A1410),  // barn-dark
    onBackground      = Color(0xFFF5E6D3),
    surface           = Color(0xFF0F0E0C),  // charcoal
    onSurface         = Color(0xFFF5E6D3),
    surfaceVariant    = Color(0xFF2A3040),  // night-teal
    onSurfaceVariant  = Color(0xFFC8B8A0),  // cream-muted
    error             = Color(0xFFE53E2C),
    onError           = Color(0xFFF5E6D3),
    outline           = Color(0xFF2E2A26),  // divider
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
