// AugerLink — root build file
// Phase 1 skeleton: Compose-only app module. Multi-module split lands in Phase 2.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.chaquopy) apply false
}
