# AugerLink Roadmap

**Status**: Draft — Phase 1 substantially complete (repo + branding + skeleton + UI shell + palette variants + sun-aware auto-flip), Phase 2 onward sketched
**Last updated**: 2026-05-03

---

## Mission

Build **AugerLink** — a sovereign mesh-comms messaging app for Android, branded and designed as a real product (Signal / Briar archetype) rather than a developer testbed. AugerLink ships validated Reticulum + LXMF capabilities (persistent identity, send / receive, voice attachments, contact CRUD, background-reliable service, wake-phrase detection) inside a coherent visual + interaction shell appropriate for farmer-in-field, off-grid, and sovereign-comms users.

Why a separate app rather than a Sideband fork or testbed promotion: the underlying Reticulum / LXMF stack is technically sound on Android, but the testbed UI signals "developer scaffolding." A real-feeling app — branded, ergonomically polished, deployment-disciplined — is what gets adoption in the Farm Hack alliance, Edge Collective, PVOS, and similar movement-aligned networks. AugerLink becomes the canonical demo + the seed for community contribution to the Reticulum Android ecosystem.

**Success means** AugerLink installable as a polished APK with branded identity (icon, splash, name), warm-dark theme tuned for outdoor / dawn / dusk readability, conversation-thread UI, multi-contact CRUD with QR import, foreground service that owns the Reticulum router (survives screen-off doze), and visible sovereign-comms posture indicators (verified-boot key hash, identity QR, peer-discovery status).

---

## Architectural decisions

| Decision | Choice | Rationale |
|---|---|---|
| UI framework | Kotlin + Jetpack Compose | Modern declarative Android UI; better tight-iteration story than Kivy/buildozer |
| Reticulum / LXMF runtime | Chaquopy embedded Python | Preserves clean upstream dependency on `rns` + `lxmf` libraries; native Kotlin port not justified |
| Local persistence | Room DB | Relational data (contacts × messages × conversations); flat-JSON DataStore was fine for the testbed but doesn't scale |
| Identity / router lifecycle | Foreground service-owned | Activity-owned router dies with screen-off / doze; service-owned survives |
| Theme | Dark warm low-blue, no light mode | Matches farmer-in-field / sleep-friendly aesthetic; concept-art-driven palette (see [`design/Auger_Link.png`](design/Auger_Link.png)) |
| Two-app architecture | Testbed continues as experimental bench; AugerLink is the curated product | Validated patterns get redesigned for actual users; testbed stays useful for tomorrow's experiments |
| Verified boot posture | Yellow (custom-keyed, e.g. GrapheneOS) recommended; works on stock Android | See [README — Sovereign-comms posture](README.md#sovereign-comms-posture-what-the-indicators-are-and-why-grapheneos) |

---

## Anti-patterns to avoid

- **Hardcoded UI automation coordinates.** Use dynamic `uiautomator dump` to discover element bounds per device + orientation; coords drift with API level, density, and notch geometry.
- **Silent ImportError fallbacks.** Always log to stderr when an optional dependency is missing — invisible degraded modes are how prod incidents start.
- **Flat JSON in DataStore for relational data.** Room DB is right for contacts × messages × conversations; DataStore is right for flat scalars like preferences.
- **Activity-owned RNS lifecycle.** The router belongs to a foreground service so it survives activity death + screen-off doze; activity is a UI client.
- **Blue-tinted dark surfaces, electric cyan accents, pure `#FFFFFF` body text.** All three are absent from the concept art's emotional register; they signal "tech minimalism" instead of "practical, working, grounded." See [`design/palette.md`](design/palette.md).
- **`Modifier.imePadding()` inside `Scaffold.bottomBar`.** The `bottomBar` slot natively respects IME insets; adding `imePadding()` double-pads the input above the keyboard. (See discussion in `app/src/main/kotlin/io/silomaceff/augerlink/ui/chats/ChatScreen.kt`.)

---

## Phase breakdown

### Phase 1 — Repo + Branding Foundation

**Goal**: Establish the AugerLink repo as a separate codebase from the testbed, with the existing brand identity (`design/Auger_Link.png` concept art) translated into committed design tokens, an Android Studio skeleton, and an installable APK with a Palette Sampler entry screen.

**Deliverables**:
- New public GitHub repo (this one)
- README with mission, sovereign-comms positioning, "what makes this different from Sideband"
- LICENSE (Apache 2.0 provisional — see [`LICENSE.notes.md`](LICENSE.notes.md))
- `design/Auger_Link.png` hero concept art committed to the repo
- `design/palette.md` — palette tokens + WCAG AA contrast verification matrix
- `design/typography.md` + `design/icon-spec.md` — typography selection + icon design spec
- App icon at multiple resolutions + adaptive icon
- Splash screen
- Android Studio Compose project skeleton with `Theme.kt` wiring palette tokens into a Material 3 dark color scheme
- `BUILD.md` with the QEMU_LD_PREFIX workaround for ARM64 Linux build hosts

**Success criteria**:
- [x] Repo created + initial commit pushed to GitHub
- [x] README renders properly with hero image visible
- [x] LICENSE chosen + committed
- [x] palette.md WCAG matrix shows all body-text + accent combos passing AA
- [ ] App icon at multiple resolutions visible on launcher with the light-arc motif recognizable (`design/icon-master.svg` craft work outstanding)
- [x] Splash screen visible on launch
- [x] Empty APK builds successfully on Pi (`./gradlew :app:assembleDebug` exits 0)
- [x] MaterialTheme correctly maps palette tokens (verified by Palette Sampler render)

### Phase 2 — Core Library Extraction (Shared Module) + Palette Variants + Auto-Flip

**Goal**: Extract validated Reticulum / LXMF / wake-detection code from the testbed into a shared library module that both apps can depend on. Plus the palette substrate work (Day-shift / Night-shift variants, sun-aware auto-flip, palette sampler dashboard) which already landed during Phase 1's iteration.

**Deliverables**:
- `:silo-comms-core` Gradle module (Kotlin library, no UI deps)
- Migrated source from the testbed: Python init, config keys, `WakeDetector`, `SpeakService`, foreground service scaffolding
- Public API: clear interfaces for `SiloCommsRouter` (init, send, receive callback), `WakeListener` (start, stop, score callback), `SpeakClient` (speak text + voice override)
- Reticulum AutoInterface monkey-patch bundled in the library — applied automatically at init (Reticulum's anti-LLM contribution policy means we carry the patch locally rather than upstream it)
- Library unit tests covering critical paths (mocked RNS layer)
- ✅ **Already shipped during Phase 1 iteration**: dual-variant palette (Night-shift / Day-shift), sun-aware auto-flip with 30 min sunset leadtime, palette sampler dashboard

**Success criteria**:
- [ ] Module builds standalone
- [ ] Testbed app updated to depend on shared module + still builds + still passes its smoke tests
- [ ] Library API surface documented in module-level KDoc
- [ ] Reticulum monkey-patch lands at first init; AutoInterface broadcast discovery works on Android (validated by send / receive between two devices on same WiFi)
- [ ] At least 5 library unit tests covering router lifecycle + send / receive + monkey-patch behavior
- [x] Day-shift + Night-shift palette variants both pass WCAG AA on all 10 fg/bg pairs
- [x] Auto-flip 30 min before sunset using a pure-Kotlin NOAA solar calculator (no network)

### Phase 3 — UI Shell with Mock Data

**Goal**: Build the messaging-app UI shell — conversation list home, conversation thread view, contacts list + detail, settings — with full visual branding and farm-themed mock data. No business logic yet; navigate-but-empty so perception-driven UI iteration can happen on a real device before LXMF wiring.

**Deliverables**:
- `ConversationListScreen` (home) — list of conversations with last-message preview
- `ChatScreen` — Signal / Telegram-style message bubbles, IME-following composer (line-growing up to 6 lines then internal scroll), routing footer
- `ContactListScreen` + `ContactDetailScreen` — verified shields, monospace destination hashes, online/last-seen state, "Open chat" CTA
- `SettingsScreen` (hosts the Palette Sampler for now)
- Bottom NavigationBar (Chats / Contacts / Settings)
- Mock data with farm-themed conversations + 5 contacts; data models shape-matched to LXMF wire format so Phase 4 is mechanical migration

**Success criteria**:
- [x] All 5 screens compose-render correctly on Pixel 6a
- [x] Phase 1 palette tokens applied consistently
- [x] Navigation between screens works (back-stack discipline correct)
- [x] Zero functional business logic — Send button is a no-op stub (Phase 4 wires LXMF send)
- [x] Models (`Contact`, `Message`, `Conversation`) shape-match LXMF wire format

### Phase 4 — Conversation Persistence (Room DB)

**Goal**: Wire AugerLink to a Room DB for contacts + messages + conversation threads. Replace the mock store with a Room-backed store preserving the same data shapes.

**Deliverables**:
- Room schema: `contacts(id, name, lxmf_hash, added_at, last_seen, color_seed)`, `messages(id, contact_id, direction, body, audio_path, voice_hint_flag, timestamp, delivered_at, read_at)`, `conversations` (joined view)
- DAO interfaces with `Flow`-returning queries for reactive Compose binding
- Mock-to-Room migration: replace `MockStore` with `RoomStore` preserving shapes
- Send path: write to messages table → fire LXMF send → on delivery confirmation update `delivered_at`
- Receive callback: write to messages table → fire `NotificationCompat.Builder` if app backgrounded → mark `read_at` on conversation-screen-foreground
- Conversation list bound to live Flow of (contact, last-message) pairs
- Chat screen bound to live Flow of messages-for-conversation

**Success criteria**:
- [ ] Room schema validated (gradle build with kapt errors=0)
- [ ] Database round-trip: add contact → send message → kill app → relaunch → conversation visible with persisted message
- [ ] Notification fires on incoming message when app backgrounded
- [ ] Tap notification → opens correct conversation thread (deep-link)

### Phase 5 — Service-Owned Crypto Identity + Background Reliability

**Goal**: Migrate the Reticulum router lifecycle from `MainActivity` into `AugerLinkService`. Service owns the LXMF identity, send / receive callbacks, AutoInterface monkey-patch, and inbox polling. Add battery-optimization whitelist onboarding + `BOOT_COMPLETED` restart receiver.

**Deliverables**:
- `AugerLinkService` with real init + send-queue + receive-loop (replacing the testbed's worker stub)
- LXMF identity loaded in service `onCreate`, not Activity
- Activity becomes a pure UI client — observes Room DB Flows + sends user actions to service via Intent or bound-service binder
- `BOOT_COMPLETED` receiver that restarts the service if user previously enabled "keep listening"
- WorkManager periodic check-in every ~30 min to verify service health (restart if killed by OEM doze)
- Battery-optimization onboarding: detect via `PowerManager.isIgnoringBatteryOptimizations` + show explanatory screen with "open settings" deep link
- Service-state observability: foreground notification shows live status (e.g., "Connected to 2 peers, 0 unread")

**Success criteria**:
- [ ] Phone screen-off + 30 min wait + bot sends message → app receives + notification fires (verified via adb logcat + dumpsys)
- [ ] Reboot phone → `BOOT_COMPLETED` → service auto-starts (verified via logcat after `adb reboot`)
- [ ] Battery-optimization whitelist onboarding visible on first launch
- [ ] Activity death (force-stop activity only) doesn't kill service — receive still works
- [ ] AutoInterface monkey-patch loaded; broadcast peer discovery works (Pi bot announces, app sees announce)

### Phase 6 — Voice + Wake Phrase + Sovereign-Comms Posture Polish

**Goal**: Wire wake-phrase detection + voice attachments + `FIELD_SPEAK_HINT` auto-speak into the UI. Surface the sovereign-comms posture indicators (verified-boot key hash, identity QR, peer-discovery status) per the [README posture section](README.md#sovereign-comms-posture-what-the-indicators-are-and-why-grapheneos).

**Deliverables**:
- Wake phrase detection ("Hey Auger" or user-customizable) integrated into the chat screen
- Voice message rendering: opus playback bubbles (in / out), `FIELD_AUDIO` incoming with auto-play setting
- `FIELD_SPEAK_HINT` auto-speak on incoming text messages with the flag set; user toggle in settings
- Identity QR generation — Settings → Identity → "Show my QR" (scannable LXMF dest hash + verified-boot key hash for cross-verification)
- "Sovereign-comms posture" indicator on home screen — green / amber / red based on the visible boot state, bootloader lock status, service health, and peer discovery
- Per-contact metadata UI — `added_at` + `last_seen` displayed in contact row + detail screen

**Success criteria**:
- [ ] Wake phrase + voice message round-trip (wake → record → send → bot reply → playback)
- [ ] `FIELD_SPEAK_HINT` incoming text auto-speaks via phone TTS
- [ ] Identity QR scannable from another device
- [ ] Verified-boot key hash matches GrapheneOS published value when running on GrapheneOS; matches Google's known key when running on stock Pixel
- [ ] Posture indicator color matches actual state across all 4 conditions

### Phase 7 — Reproducible Build + Distribution

**Goal**: Make AugerLink reproducibly buildable on a Raspberry Pi 500 + ship a signed release APK. Document the build for a third-party contributor.

**Deliverables**:
- Expanded `BUILD.md` with one-command build instructions (curl-able install for the toolchain prereqs, then `./gradlew :app:assembleRelease`)
- [reproducible-builds.org](https://reproducible-builds.org/) compliance — pinned dependency versions, deterministic build flags
- Signing key generation + secure storage doc (NOT in repo)
- Release APK signed with sovereign key (not debug)
- F-Droid metadata file (fastlane format) for future inclusion in F-Droid IzzyOnDroid or main repo
- GitHub Actions workflow for build + APK artifact attachment to release tags
- Pi-side build verification script — clones fresh, builds, diffs APK against committed sha256

**Success criteria**:
- [ ] BUILD.md walks a fresh contributor from blank Pi to working APK in < 30 min
- [ ] Two independent builds on Pi produce byte-identical APKs (or documented variance ≤ signing + timestamp blocks)
- [ ] Release APK installs on Pixel 6a + GrapheneOS + smoke test passes
- [ ] F-Droid metadata validates against fastlane schema
- [ ] GitHub Actions build green on a tag push

---

## Status

Phase 1 is substantially complete. Phase 2 is partially complete (palette variants + auto-flip already shipped during Phase 1 iteration; library extraction outstanding). Phase 3 is complete (UI shell + mock data shipped on Pixel 6a). Phase 4 onward is the next concrete work.

Open questions, design feedback, contribution interest — please file an issue: <https://github.com/silomaceff-d0cdcf/auger-link/issues>
