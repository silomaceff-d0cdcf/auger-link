# Building AugerLink

This document covers Phase 1 (debug-build skeleton). Reproducible release-build
docs land in Phase 7 (`Reproducible Build + Distribution`).

## Quick start

```bash
git clone https://github.com/silomaceff-d0cdcf/auger-link
cd auger-link

# Build debug APK
./gradlew :app:assembleDebug

# APK lands at:
# app/build/outputs/apk/debug/app-debug.apk
```

## Prerequisites

- **JDK 17+** (`openjdk-17-jdk-headless` on Debian/Ubuntu)
- **Android SDK** with platform `android-34` and `build-tools 34.0.0`
- **`local.properties`** with `sdk.dir=/path/to/android-sdk` (gitignored — per-machine)

## ARM64 Linux build hosts (Raspberry Pi, Apple Silicon Asahi, ARM servers)

Google ships Android SDK build-tools as **x86_64-Linux only** (no `linux-aarch64` variant). Running `./gradlew :app:assembleDebug` on an ARM64 host hits AAPT2 with errors like:

```
x86_64-binfmt-P: Could not open '/lib64/ld-linux-x86-64.so.2': No such file or directory
AAPT2 ... Daemon startup failed
```

**Fix on Debian/Ubuntu ARM64**:

```bash
sudo apt install qemu-user-static binfmt-support libc6-amd64-cross
export QEMU_LD_PREFIX=/usr/x86_64-linux-gnu

./gradlew :app:assembleDebug
```

The `QEMU_LD_PREFIX` exposes the cross-compiled libc6 + dynamic-linker so qemu-user-static can run the x86_64 AAPT2 binary under emulation. Add the export to your shell profile (`~/.bashrc`) for persistence.

Reference: agent learning `2026-04-18_000500_android_build_tools_x86_64_only_on_linux_learning.md`.

First build downloads gradle 8.9 (~120MB), AGP 8.7.3, Compose BOM 2024.11, and dependencies — expect ~5min on Pi 500. Subsequent builds are <1min thanks to gradle config-cache + build-cache.

## Building on x86_64 Linux / macOS / Windows

No env tweaks needed — Android build-tools run natively. The `QEMU_LD_PREFIX` hack only applies to ARM64 Linux hosts.

## What's in this build (Phase 1)

The Phase 1 skeleton is a **palette sampler app** — opening it shows every brand color token as it'll appear in real Material 3 components on real device hardware. Includes:

- 11 named color tokens with hex labels and role descriptions
- 5 button states (filled, tonal, outlined, text, destructive)
- Incoming + outgoing message bubble preview
- Status indicators (connected, error, active-listening dot)
- Hash-display in monospace
- TopAppBar, FAB, dividers, and adaptive launcher icon

Side-load + open the APK to verify the palette renders as intended in ambient farm-edge / dawn / dusk light. WCAG AA verification of fg/bg pairs is automated in `design/wcag_verify.py` (10/10 pass; run `python3 design/wcag_verify.py --strict` before any palette edit).

## APK install

```bash
# Push to a connected Android device:
adb install -r app/build/outputs/apk/debug/app-debug.apk

# App appears as "AugerLink" in the launcher.
```
