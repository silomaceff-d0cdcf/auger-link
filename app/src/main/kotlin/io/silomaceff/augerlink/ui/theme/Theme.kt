package io.silomaceff.augerlink.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/** Selectable palette variant. Default is Night (harvest-warm). Day is cool-mute. */
enum class PaletteVariant(val displayName: String) {
    Night("Night-shift"),
    Day("Day-shift"),
}

private fun colorSchemeFor(tokens: AugerLinkPaletteTokens) = darkColorScheme(
    primary              = tokens.primary,
    onPrimary            = tokens.onPrimary,
    primaryContainer     = tokens.primaryContainer,
    onPrimaryContainer   = tokens.onPrimaryContainer,
    secondary            = tokens.secondary,
    onSecondary          = tokens.background,
    secondaryContainer   = tokens.surfaceVariant,
    onSecondaryContainer = tokens.onBackground,
    background           = tokens.background,
    onBackground         = tokens.onBackground,
    surface              = tokens.surface,
    onSurface            = tokens.onBackground,
    surfaceVariant       = tokens.surfaceVariant,
    onSurfaceVariant     = tokens.onSurfaceVariant,
    error                = tokens.error,
    onError              = tokens.onBackground,
    outline              = tokens.divider,
)

/** Composition-local so any composable can read the active palette tokens
 *  without threading them through every parameter. Defaults to Night.
 */
val LocalAugerLinkTokens = staticCompositionLocalOf<AugerLinkPaletteTokens> { AugerLinkColorsNight }

/**
 * AugerLink theme — dark only, two palette variants.
 *
 * @param variant select Night (harvest) or Day (cool-mute). Default Night.
 * @param darkTheme accepted for API symmetry but ignored — v1 is dark-only.
 */
@Composable
fun AugerLinkTheme(
    variant: PaletteVariant = PaletteVariant.Night,
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val tokens: AugerLinkPaletteTokens = when (variant) {
        PaletteVariant.Night -> AugerLinkColorsNight
        PaletteVariant.Day -> AugerLinkColorsDay
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalAugerLinkTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorSchemeFor(tokens),
            typography = AugerLinkTypography,
            content = content,
        )
    }
}
