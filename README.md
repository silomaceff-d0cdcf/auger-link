# AugerLink

**Sovereign mesh-comms messaging for Android.** Built for off-grid, farm-edge, low-trust deployment.

![AugerLink concept art — dusk farm with grain-silo-as-mesh-tower + glowing comms-arc to the phone in a gloved hand](design/Auger_Link.png)

## What this is

AugerLink is a native Android messaging app that runs over [Reticulum](https://reticulum.network) + [LXMF](https://github.com/markqvist/LXMF) — a cryptography-based networking stack designed for unstoppable peer-to-peer communication over LoRa, packet radio, WiFi, and any other transport you can plug into it.

Where Sideband is the mature flagship of the Reticulum Android ecosystem, AugerLink is a sister app with a different posture:

- **Farmer-and-field aesthetic** — dark warm UI tuned for outdoor / dawn / dusk readability, NOT cold blue-tech minimalism
- **Curated feature surface** — voice messaging via wake-phrase, multi-contact CRUD, reliable background reception, sovereign verifiable boot posture
- **Sovereign-comms posture indicators** — visible verified-boot key hash, identity QR, peer-discovery status, all surfaced in-app so users can verify the chain of trust at a glance
- **Reproducible build on a Raspberry Pi 500** — anyone with a Pi + GrapheneOS-capable Pixel can build and ship the same APK

## Why a separate app

AugerLink was extracted from a working testbed (`chaquopy-silo-hello`) where we validated that Compose + Kotlin + Chaquopy embedded Python + RNS/LXMF can ship a real Android app on top of the Reticulum stack. The testbed continues to exist as an experimental playground; AugerLink is the curated product where validated patterns get redesigned for actual users — farmers, off-grid communities, mesh-radio enthusiasts, anyone who wants comms that don't depend on a tower.

## Status

**Phase 1: Repo + Branding Foundation** (in progress).

7-phase MISSION roadmap is at `agent/public/roadmaps/2026-05-01_AugerLink_Sovereign_Comms_Messaging_App_curated_rebuild_from_51_testbed/roadmap.md` (in the agent's repo; will be linked here once published).

## Brand

The hero concept art at [`design/Auger_Link.png`](design/Auger_Link.png) is the brand foundation — established before code; visual language flows from the image.

The auger as a farmer's grain-handling tool, repurposed in the visual metaphor as a wireless mesh-comms primitive: a literal grain silo standing as a comms tower, glowing with the trace of LXMF packets in flight to the phone in the field. That's the mark.

Color palette extracted from the concept art is in [`design/palette.md`](design/palette.md) (in progress). Typography in [`design/typography.md`](design/typography.md) (TBD). App icon spec in [`design/icon-spec.md`](design/icon-spec.md) (TBD).

## License

[Apache License 2.0](LICENSE) — provisional. Permissive + explicit patent grant + upstream-compatible with Reticulum/LXMF/Sideband (all MIT). Revisit conditions and decision rationale in [`LICENSE.notes.md`](LICENSE.notes.md) — input from Don Blair / Dorn Cox / Farm Hack alliance still welcome and may move us toward copyleft (GPL-3.0) if the movement-aligned posture matters more than ecosystem permissiveness.

Copyright 2026 Craig Versek and AugerLink contributors.

## Build

`BUILD.md` lands in Phase 7. For now:

```
# Prerequisites: gradle 8.11+, openjdk-17-jdk, Android SDK + NDK,
# qemu-user-static + libc6-amd64-cross + QEMU_LD_PREFIX for ARM64 hosts
gradle :app:assembleDebug
```

## Sister projects

- [`chaquopy-silo-hello`](../chaquopy-silo-hello/) — testbed app, continues as experimental playground
- [Reticulum](https://reticulum.network) — upstream networking stack (we depend on it; we don't fork it)
- [LXMF](https://github.com/markqvist/LXMF) — upstream message format
- [GrapheneOS](https://grapheneos.org) — recommended sovereign-comms-grade Android distribution for AugerLink deployment

---

*Built by SiloMacEff@d0cdcf with [MacEff framework](https://github.com/cversek/MacEff). Full-spectrum collaboration between Craig Versek (PM/architect) and Silo (agent).*
