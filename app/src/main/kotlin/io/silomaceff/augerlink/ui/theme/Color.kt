package io.silomaceff.augerlink.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * AugerLink palette tokens. Two variants exist:
 *
 *  - [AugerLinkColorsNight] — "night-shift" / harvest. Warm-amber-dominant,
 *    pulled directly from the dusk-farm hero concept art. Warm dark, gold +
 *    glow-orange + dusk-amber. Eye-comfortable for evening / dawn / dusk
 *    farm-edge use.
 *
 *  - [AugerLinkColorsDay] — "day-shift" / cool-mute. Same brand spine
 *    cooled-and-muted: weathered-bronze instead of pumpkin-amber, straw-tan
 *    instead of marigold gold, linen instead of cream, slight-blue-undertone
 *    very-dark background. Year-round usable; reads less Halloween / less
 *    seasonal-coded for non-farmer users (mesh-radio enthusiasts, urban
 *    sovereign-comms users).
 *
 * Both variants are dark-only (no light theme in v1). Both pass WCAG AA on
 * all 10 documented fg/bg pairs (verified by `design/wcag_verify.py
 * --variant {night-shift|day-shift} --strict`).
 */

interface AugerLinkPaletteTokens {
    // Brand warms / accents
    val primary: Color
    val primaryContainer: Color
    val secondary: Color

    // Surfaces
    val background: Color
    val surface: Color
    val surfaceVariant: Color

    // Text
    val onBackground: Color
    val onSurfaceVariant: Color
    val onPrimary: Color
    val onPrimaryContainer: Color

    // Semantic
    val error: Color
    val success: Color

    // Outline
    val divider: Color
}

/** Night-shift / harvest palette — warm amber-dominant. Source: hero concept art. */
object AugerLinkColorsNight : AugerLinkPaletteTokens {
    override val primary            = Color(0xFF9C5519)  // dusk-amber (WCAG-verified, was #B5651D)
    override val primaryContainer   = Color(0xFFD97826)  // glow-orange
    override val secondary          = Color(0xFFFFC065)  // gold

    override val background         = Color(0xFF1A1410)  // barn-dark (warm)
    override val surface            = Color(0xFF0F0E0C)  // charcoal (warm)
    override val surfaceVariant     = Color(0xFF2A3040)  // night-teal

    override val onBackground       = Color(0xFFF5E6D3)  // cream
    override val onSurfaceVariant   = Color(0xFFC8B8A0)  // cream-muted
    override val onPrimary          = Color(0xFFF5E6D3)  // cream — 4.60:1 ✅
    override val onPrimaryContainer = Color(0xFF1A1410)  // barn-dark — 5.79:1 ✅

    override val error              = Color(0xFFE53E2C)  // warm red
    override val success            = Color(0xFF7FA756)  // muted olive

    override val divider            = Color(0xFF2E2A26)  // warm-dark divider
}

/** Day-shift / cool-mute palette — same brand spine, less seasonal. */
object AugerLinkColorsDay : AugerLinkPaletteTokens {
    override val primary            = Color(0xFF9C7A52)  // weathered bronze (vs pumpkin amber)
    override val primaryContainer   = Color(0xFFB5896A)  // oak-tan (vs glow-orange)
    override val secondary          = Color(0xFFC4A77D)  // straw-tan (vs marigold gold)

    override val background         = Color(0xFF15181B)  // cool-neutral very-dark, slight blue undertone
    override val surface            = Color(0xFF0E1114)  // deeper cool-neutral
    override val surfaceVariant     = Color(0xFF2C353D)  // cooler than night-teal

    override val onBackground       = Color(0xFFE0DCD2)  // linen (less warm than cream)
    override val onSurfaceVariant   = Color(0xFFA8A498)  // taupe (less amber)
    override val onPrimary          = Color(0xFF15181B)  // bg-dark — 4.51:1 ✅
    override val onPrimaryContainer = Color(0xFF15181B)  // bg-dark — 5.73:1 ✅

    override val error              = Color(0xFFD85A4F)  // coral (less stop-sign)
    override val success            = Color(0xFF86A88A)  // sage-green (less olive-warm)

    override val divider            = Color(0xFF262A30)  // cool divider
}

/** Default — kept as a stable alias for code that doesn't switch variants. */
val AugerLinkColors: AugerLinkPaletteTokens = AugerLinkColorsNight
