# AugerLink

**Sovereign mesh-comms messaging for Android.** Built for off-grid, farm-edge, low-trust deployment.

![AugerLink concept art — dusk farm with grain-silo-as-mesh-tower + glowing comms-arc to the phone in a gloved hand](design/Auger_Link.png)

## What this is

AugerLink is a native Android messaging app that runs over [Reticulum](https://reticulum.network) + [LXMF](https://github.com/markqvist/LXMF) — a cryptography-based networking stack designed for unstoppable peer-to-peer communication over LoRa, packet radio, WiFi, and any other transport you can plug into it.

Where Sideband is the mature flagship of the Reticulum Android ecosystem, AugerLink is a sister app with a different posture:

- **Farmer-and-field aesthetic** — dark warm UI tuned for outdoor / dawn / dusk readability, NOT cold blue-tech minimalism
- **Curated feature surface** — voice messaging via wake-phrase, multi-contact CRUD, reliable background reception, sovereign-comms posture indicators
- **Sovereign-comms posture indicators** — when running on a verified-boot Android (e.g. GrapheneOS), the app surfaces verified-boot key hash, identity QR, peer-discovery status in-app so users can verify the chain of trust at a glance. On stock Android the app still runs; the posture indicators degrade gracefully to "unverified."
- **Reproducible build on a Raspberry Pi 500** — toolchain runs on any Linux host (Pi 500, x86_64 Linux, Apple Silicon via Rosetta); install target is any Android 10+ device (sideload via `adb install` or unknown-sources)

## Sovereign-comms posture: what the indicators are, and why GrapheneOS

A **posture indicator** is a UI element that surfaces the device's actual security and configuration state to the user instead of hiding it. Most apps and operating systems hide posture; AugerLink argues that posture should be first-class visible because users running sovereign-comms need to know they can trust the device they're using.

Phase 5 will surface, in-app:

- **Verified-boot key hash** — a short fingerprint of the public key the phone's bootloader is enforcing at boot. You can match this against a known-good published hash; tampering becomes visible.
- **Boot state** — `ro.boot.verifiedbootstate`: GREEN (locked + OEM-signed), ORANGE (unlocked, no verification), YELLOW (locked + custom-keyed, e.g. GrapheneOS), RED (tamper detected).
- **Identity QR** — your LXMF destination as a scannable QR for in-person peer verification (out-of-band trust establishment).
- **Peer-discovery status** — which mesh / radio / local interfaces are up and which peers are visible right now.

The indicators don't enforce a posture — they surface it. The user (and any peer they hand the phone to) can judge trustworthiness from the visible state.

### Why we recommend GrapheneOS over stock Google Pixel

Concrete, not religious:

1. **Bootloader key ownership.** Stock Pixel ships with the bootloader locked against Google's signing key — you trust Google not to be coerced (national-security letter with gag) or supply-chain compromised. GrapheneOS locks the bootloader against its own published key, fingerprint shown at every boot, matchable against the published value. You can verify the device is running the OS you think it's running.

2. **No Google services in a privileged position.** Stock Pixel ships Google Mobile Services, Pixel telemetry, and Google account integration as first-class system components. GrapheneOS sandboxes Google Play Services into a normal user profile (or omits them entirely). Removing Google's privileged position eliminates an attack surface AND a legal-compulsion surface.

3. **Hardening above the AOSP baseline.** Hardened malloc, kernel exploit mitigations beyond stock, per-app NETWORK and SENSORS permission toggles (apps must explicitly request network access; granular sensor control), Storage Scopes instead of all-or-nothing storage permissions. Empirically more resistant to remote exploit chains.

4. **Update transparency.** GrapheneOS ports Pixel security patches plus its own; the project is small-team with publicly auditable source. You can read the patches that protect you.

**What stock Pixel still has.** Identical Titan M2 hardware-backed security (this is a Pixel hardware feature, not an OS-specific one). Easier setup for non-technical users. Full Play Store and unsandboxed Google services if you want them.

**AugerLink works on stock Android.** The posture indicator will read "Google-signed boot" instead of "GrapheneOS-signed boot." Both are real cryptographic guarantees; the GrapheneOS one is verifiable against a published key (you can match the fingerprint), the Google one is "the key Google uses" with no public audit possible. For users whose threat model includes large-vendor coercion or supply-chain compromise, the GrapheneOS posture is preferred. For users whose threat model is "I just want comms that don't depend on a tower," stock Android is fine — and AugerLink will run identically with reduced posture certainty.

## Why a separate app

AugerLink was extracted from a working testbed (`chaquopy-silo-hello`) where we validated that Compose + Kotlin + Chaquopy embedded Python + RNS/LXMF can ship a real Android app on top of the Reticulum stack. The testbed continues to exist as an experimental playground; AugerLink is the curated product where validated patterns get redesigned for actual users — farmers, off-grid communities, mesh-radio enthusiasts, anyone who wants comms that don't depend on a tower.

## Status

**Phase 1: Repo + Branding Foundation** (in progress).

Full 7-phase roadmap (mission, architectural decisions, phase deliverables + success criteria) is in [`ROADMAP.md`](ROADMAP.md). Phase 2 + 3 already partially complete (palette variants + auto-flip + UI shell with mock data shipped during Phase 1 iteration); Phase 4 onward is the next concrete work.

## Brand

The hero concept art at [`design/Auger_Link.png`](design/Auger_Link.png) is the brand foundation — established before code; visual language flows from the image.

The auger as a farmer's grain-handling tool, repurposed in the visual metaphor as a wireless mesh-comms primitive: a literal grain silo standing as a comms tower, glowing with the trace of LXMF packets in flight to the phone in the field. That's the mark.

Color palette extracted from the concept art is in [`design/palette.md`](design/palette.md) (in progress). Typography in [`design/typography.md`](design/typography.md) (TBD). App icon spec in [`design/icon-spec.md`](design/icon-spec.md) (TBD).

## License

[Apache License 2.0](LICENSE) — provisional. Permissive + explicit patent grant + upstream-compatible with Reticulum/LXMF/Sideband (all MIT). Revisit conditions and decision rationale in [`LICENSE.notes.md`](LICENSE.notes.md) — input from the Farm Hack alliance still welcome and may move us toward copyleft (GPL-3.0) if the movement-aligned posture matters more than ecosystem permissiveness.

Copyright 2026 Craig Versek and AugerLink contributors.

## Build & Install

**Build host** — any Linux box with the Android SDK + JDK 17 + Gradle wrapper. The repo has been validated on a Raspberry Pi 500 (ARM64 Linux); see [`BUILD.md`](BUILD.md) for the QEMU_LD_PREFIX workaround needed when running x86_64 AAPT2 on an ARM64 host. x86_64 Linux is the smoothest path; Apple Silicon works via Rosetta 2.

```
# Prerequisites: openjdk-17-jdk-headless, Android SDK + build-tools,
# the Gradle wrapper handles the rest
./gradlew :app:assembleDebug
# APK lands at app/build/outputs/apk/debug/app-debug.apk
```

**Install target** — any Android 10+ device (API 29+). Sideload via:

```
adb install -r app/build/outputs/apk/debug/app-debug.apk
# or transfer the APK and install via Android's "install from unknown sources"
```

The app runs on stock Android. GrapheneOS / verified-boot Android is *recommended* for the strongest sovereign-comms posture (the in-app posture indicators are most informative there) but not required.

## Sister projects

- `chaquopy-silo-hello` — internal testbed app where Compose + Chaquopy + RNS / LXMF integration was first validated; continues as an experimental bench (not currently public)
- [Reticulum](https://reticulum.network) — upstream networking stack (we depend on it; we don't fork it)
- [LXMF](https://github.com/markqvist/LXMF) — upstream message format
- [GrapheneOS](https://grapheneos.org) — recommended (but optional) sovereign-comms-grade Android distribution; AugerLink runs on stock Android and degrades the posture indicators gracefully

---

*Built by SiloMacEff@d0cdcf with [MacEff framework](https://github.com/cversek/MacEff). Full-spectrum collaboration between Craig Versek (PM/architect) and Silo (agent).*
