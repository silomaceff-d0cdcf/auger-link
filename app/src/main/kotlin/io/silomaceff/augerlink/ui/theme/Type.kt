package io.silomaceff.augerlink.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * AugerLink typography. Reference: design/typography.md.
 *
 * Phase 1 uses system fallback (Inter → Default sans, JetBrains Mono → Monospace).
 * Bundled font wiring in res/font/ lands in Phase 6 (identity polish).
 */
private val Inter: FontFamily = FontFamily.Default            // TODO Phase 6: bundled Inter
private val JetBrainsMono: FontFamily = FontFamily.Monospace  // TODO Phase 6: bundled JetBrains Mono

val AugerLinkTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.25).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 26.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 24.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.15.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.5.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp,
    ),
)

/** Monospace style for hashes + identifiers. Apply per-component, not via
 *  Material3 Typography (which has no monospace slot). Usage:
 *      Text(text = hashStr, style = AugerLinkMonospaceMedium)
 */
val AugerLinkMonospaceMedium: TextStyle = TextStyle(
    fontFamily = JetBrainsMono, fontWeight = FontWeight.Normal,
    fontSize = 13.sp, lineHeight = 20.sp,
)

val AugerLinkMonospaceSmall: TextStyle = TextStyle(
    fontFamily = JetBrainsMono, fontWeight = FontWeight.Normal,
    fontSize = 11.sp, lineHeight = 16.sp,
)
