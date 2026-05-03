package io.silomaceff.augerlink.ui.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import io.silomaceff.augerlink.ui.PaletteSamplerScreen
import io.silomaceff.augerlink.ui.theme.PaletteLocation
import io.silomaceff.augerlink.ui.theme.PaletteMode
import io.silomaceff.augerlink.ui.theme.PaletteVariant

/**
 * Settings tab — currently re-uses the Phase 1 [PaletteSamplerScreen] as
 * its full body so the palette sampler + dashboard remains accessible from
 * the running app (now nested as Settings rather than the entire app).
 *
 * Phase 6 splits this into discrete sub-screens (Theme, Identity, Build
 * info, etc.). The sampler will graduate to a `Theme` settings detail
 * page reachable from this screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    mode: PaletteMode,
    resolvedVariant: PaletteVariant,
    location: PaletteLocation,
    onModeChange: (PaletteMode) -> Unit,
) {
    PaletteSamplerScreen(
        mode = mode,
        resolvedVariant = resolvedVariant,
        location = location,
        onModeChange = onModeChange,
    )
}
