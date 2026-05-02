# AugerLink App Icon Specification

**Source mark**: the glowing amber light-arc spanning between the phone (gloved-hand foreground) and the grain-silo-as-comms-tower (background), captured in the hero concept art at [`Auger_Link.png`](Auger_Link.png).

The icon is the **light-arc** — a sweeping, slightly-spiraling beam of warm amber light, rendered as a single graphic mark abstracted from the scene.

---

## Concept

A simple, recognizable amber arc rising from a stylized grain-silo base on the left, curving up-and-right and terminating in a small phone-shape on the right. Background is the warm dark `barn-dark` (`#1A1410`).

Alt-direction (lighter on form): just the arc itself, freestanding, no silo or phone — pure curve of amber-orange-gold light against warm dark, with implied directionality (thicker/brighter at the silo end, tapering to thinner/lighter at the phone end). This reads as "beam in motion" and is more recognizable at small sizes (24dp launcher badge, status bar).

Both options preserve the brand emotional register: warm, practical, in-flight, real.

**Recommended**: start with arc-only (cleaner for icon). Reserve silo-and-phone variant for promo art / adaptive-icon-foreground / splash use.

---

## Required sizes

Standard Android adaptive icon (Android 8+) with foreground + background layers:

| Layer | Format | Source size | Purpose |
|---|---|---|---|
| Foreground | PNG, transparent | 432x432 px (108dp at xxxhdpi) | The arc graphic, centered with safe zone (66dp inner circle visible across all mask shapes) |
| Background | PNG or solid color | 432x432 px | Solid `barn-dark` `#1A1410` |
| Monochrome (Android 13+ themed icons) | PNG | 432x432 px, single-color (white) | Arc shape filled white; system tints to user's accent |

Plus legacy bitmap mipmap fallbacks for pre-O devices (API < 26):

| Density | Size | Path |
|---|---|---|
| mdpi | 48x48 | `app/src/main/res/mipmap-mdpi/ic_launcher.png` |
| hdpi | 72x72 | `app/src/main/res/mipmap-hdpi/ic_launcher.png` |
| xhdpi | 96x96 | `app/src/main/res/mipmap-xhdpi/ic_launcher.png` |
| xxhdpi | 144x144 | `app/src/main/res/mipmap-xxhdpi/ic_launcher.png` |
| xxxhdpi | 192x192 | `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` |

For Play Store + project README hero badge:

| Use | Size | Format |
|---|---|---|
| Play Store icon | 512x512 | PNG, 32-bit |
| GitHub README badge | 256x256 | PNG (or SVG for scalability) |
| Notification icon | 24x24 vector | XML drawable, white-on-transparent (Android adds tint) |

---

## Color tokens (from `palette.md`)

| Element | Token | Hex |
|---|---|---|
| Arc gradient start (silo end, brightest) | `gold` | `#FFC065` |
| Arc gradient mid | `glow-orange` | `#D97826` |
| Arc gradient end (phone end, slightly dimmer) | `dusk-amber` | `#B5651D` |
| Background layer | `barn-dark` | `#1A1410` |
| Optional silo-base silhouette (if included) | `charcoal` | `#0F0E0C` (subtle, just suggests form) |

Gradient direction: along the arc length, smooth transition. Optional **glow** outer-stroke at 20-30% opacity blends into the dark background to suggest light-bleed (matches the painterly quality of the concept art).

---

## Design Constraints

- **No text in icon** — "AugerLink" wordmark is separate (used in splash, navigation header, README); icon is purely the arc mark.
- **Single-color rendering at 24dp** — must stay recognizable when system renders as monochrome (notification small-icon, themed icons). Test by exporting white-on-transparent at 24x24 and verifying the arc shape is still legible.
- **Safe zone discipline** — adaptive-icon foreground must have all critical pixels inside the 66dp inner circle. Some launcher masks crop the corners; arc start/end points should NOT extend into the masked zone.
- **No fine detail at small sizes** — arc thickness should be at least 6 px at 48x48 (mdpi) so it's not invisible. Use bolder strokes than aesthetically tempting at the design size; small-size legibility wins.

---

## Source format

Master in `design/icon-master.svg` (TBD — Phase 1 follow-up). Vector source enables clean export at all sizes. Recommended tool: Inkscape (Pi-friendly) or Figma if cross-device editing matters.

Export pipeline (Phase 1 follow-up):

```bash
# Inkscape CLI for batch export
inkscape design/icon-master.svg --export-type=png \
    --export-area-page \
    --export-width=512 \
    --export-filename=design/icon-512.png

for size in 48 72 96 144 192 256; do
  inkscape design/icon-master.svg --export-type=png \
      --export-area-page \
      --export-width=$size \
      --export-filename=app/src/main/res/mipmap-*/ic_launcher.png  # density-mapped
done
```

Adaptive icon foreground+background generated via Android Studio's "Image Asset Studio" (or manual XML drawable composition).

---

## Acceptance criteria for "icon ready"

- [ ] `design/icon-master.svg` committed (vector source)
- [ ] All 5 mipmap densities populated in `app/src/main/res/mipmap-*/ic_launcher.png`
- [ ] Adaptive icon foreground + background drawables in `app/src/main/res/mipmap-anydpi-v26/`
- [ ] Monochrome themed-icon variant in `app/src/main/res/mipmap-anydpi-v26/ic_launcher_monochrome.xml`
- [ ] Visual sanity check: install APK on Pixel 6a / GrapheneOS, confirm icon renders correctly on launcher + recents + notification small-icon
- [ ] 512x512 Play Store icon at `design/play-store-icon.png` (used in F-Droid metadata too)

---

## Anti-patterns

- ❌ Photorealistic phone+silo+farm in icon — too detailed for small sizes; loses clarity at 24dp
- ❌ Using the full hero concept art as the icon — it's a painting, not a mark; doesn't render at 48x48
- ❌ Cool-toned gradients (cyan→blue) for the arc — clashes with the brand
- ❌ Multi-character wordmark embedded in the icon — "AL" or "AugerLink" text inside the bounds is corporate-cliché and unreadable at small sizes
- ❌ Drop shadows or 3D effects — flat with subtle gradient is more in line with current Material/Android aesthetics + matches palette warmth
