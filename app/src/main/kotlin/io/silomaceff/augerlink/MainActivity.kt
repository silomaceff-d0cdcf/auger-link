package io.silomaceff.augerlink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.silomaceff.augerlink.ui.PaletteSamplerScreen
import io.silomaceff.augerlink.ui.theme.AugerLinkTheme

/**
 * Phase 1 entry point — palette sampler is the whole app.
 *
 * Phase 2+ replaces this with the real conversation-list / contact-list / etc.
 * For now: side-load + open + see every brand token rendered in real
 * Material 3 components on real device hardware.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AugerLinkTheme {
                PaletteSamplerScreen()
            }
        }
    }
}
