package io.silomaceff.augerlink.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * AugerLink palette tokens. WCAG 2.1 verified — see design/wcag_verify.py +
 * design/wcag_verified.md. 10/10 documented fg/bg pairs pass AA.
 *
 * Dark only. Warm-shifted (NOT blue-black tech minimalism). Source of truth:
 * design/palette.md.
 */
object AugerLinkColors {
    // Primary brand warm — WCAG-verified value (was #B5651D; darkened to clear cream-on-amber@4.5:1)
    val DuskAmber = Color(0xFF9C5519)

    // Accents
    val GlowOrange = Color(0xFFD97826)        // primaryContainer / outgoing-bubble
    val Gold = Color(0xFFFFC065)              // secondary / active-indicator

    // Surfaces (dark, warm — not blue-black)
    val BarnDark = Color(0xFF1A1410)          // background
    val Charcoal = Color(0xFF0F0E0C)          // surface
    val NightTeal = Color(0xFF2A3040)         // surfaceVariant — low-saturation muted blue + warm-shifted

    // Text (warm white, never #FFFFFF)
    val Cream = Color(0xFFF5E6D3)             // onBackground / onSurface
    val CreamMuted = Color(0xFFC8B8A0)        // onSurfaceVariant / secondary text

    // Semantic
    val ErrorWarm = Color(0xFFE53E2C)
    val SuccessOlive = Color(0xFF7FA756)

    // Outline
    val Divider = Color(0xFF2E2A26)
}
