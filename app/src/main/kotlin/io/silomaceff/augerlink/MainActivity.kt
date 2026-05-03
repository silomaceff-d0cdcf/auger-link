package io.silomaceff.augerlink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.silomaceff.augerlink.ui.PaletteSamplerScreen
import io.silomaceff.augerlink.ui.theme.AugerLinkTheme
import io.silomaceff.augerlink.ui.theme.PaletteVariant

/**
 * Phase 1 entry point — palette sampler is the whole app.
 *
 * Phase 2+ replaces this with the real conversation-list / contact-list / etc.
 * For now: side-load + open + see every brand token rendered in real
 * Material 3 components. Toggle between Night-shift (harvest-warm) and
 * Day-shift (cool-mute) palette variants from the top bar.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            // Saveable so a config change (rotation) doesn't snap back to default.
            var variant by rememberSaveable { mutableStateOf(PaletteVariant.Night) }
            AugerLinkTheme(variant = variant) {
                PaletteSamplerScreen(
                    variant = variant,
                    onVariantChange = { variant = it },
                )
            }
        }
    }
}
