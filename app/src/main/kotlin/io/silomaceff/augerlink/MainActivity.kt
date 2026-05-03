package io.silomaceff.augerlink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.silomaceff.augerlink.ui.PaletteSamplerScreen
import io.silomaceff.augerlink.ui.theme.AugerLinkTheme
import io.silomaceff.augerlink.ui.theme.PaletteLocation
import io.silomaceff.augerlink.ui.theme.PaletteMode
import io.silomaceff.augerlink.ui.theme.rememberResolvedVariant

/**
 * Phase 1 entry point — palette sampler is the whole app.
 *
 * Default mode is [PaletteMode.Auto]: rendered variant is Day-shift during
 * daylight, flipping to Night-shift 30 minutes before local sunset (NOAA
 * Solar Calculator algorithm in `util/SunCalc.kt`). User can override to
 * fixed Day or fixed Night via the SegmentedButton at the top of the
 * sampler. Override survives rotation via rememberSaveable.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            var mode by rememberSaveable { mutableStateOf(PaletteMode.Auto) }
            val location = PaletteLocation.Default
            val variant = rememberResolvedVariant(mode = mode, location = location)
            AugerLinkTheme(variant = variant) {
                PaletteSamplerScreen(
                    mode = mode,
                    resolvedVariant = variant,
                    location = location,
                    onModeChange = { mode = it },
                )
            }
        }
    }
}
