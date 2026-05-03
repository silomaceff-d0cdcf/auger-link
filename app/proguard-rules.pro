# Proguard rules for AugerLink (Phase 1 — placeholder, real rules land Phase 7).

# Keep Compose runtime/preview reachable for now.
-keep class androidx.compose.** { *; }

# Phase 2+ adds rules for Reticulum/LXMF native bits.
