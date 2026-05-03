package io.silomaceff.augerlink.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * AugerLink Material 3 dark color scheme. Reference: design/palette.md.
 * WCAG verification: design/wcag_verified.md (10/10 pairs pass AA).
 */
private val AugerLinkDarkColorScheme = darkColorScheme(
    primary              = AugerLinkColors.DuskAmber,
    onPrimary            = AugerLinkColors.Cream,        // 4.60:1 on DuskAmber ✅
    primaryContainer     = AugerLinkColors.GlowOrange,
    onPrimaryContainer   = AugerLinkColors.BarnDark,     // 5.79:1 on GlowOrange ✅ (cream would fail AA)
    secondary            = AugerLinkColors.Gold,
    onSecondary          = AugerLinkColors.BarnDark,
    secondaryContainer   = AugerLinkColors.NightTeal,
    onSecondaryContainer = AugerLinkColors.Cream,
    background           = AugerLinkColors.BarnDark,
    onBackground         = AugerLinkColors.Cream,        // 14.89:1 ✅
    surface              = AugerLinkColors.Charcoal,
    onSurface            = AugerLinkColors.Cream,        // 15.75:1 ✅
    surfaceVariant       = AugerLinkColors.NightTeal,
    onSurfaceVariant     = AugerLinkColors.CreamMuted,
    error                = AugerLinkColors.ErrorWarm,
    onError              = AugerLinkColors.Cream,
    outline              = AugerLinkColors.Divider,
)

/**
 * AugerLink theme — dark only, warm-amber palette per concept art.
 *
 * The `darkTheme` parameter is accepted for API symmetry but ignored — v1
 * is dark-only. Phase 6 (identity polish) may add an opt-in light variant
 * if user research surfaces demand.
 */
@Composable
fun AugerLinkTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AugerLinkDarkColorScheme,
        typography = AugerLinkTypography,
        content = content,
    )
}
