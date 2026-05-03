# LICENSE choice — provisional rationale + revisit conditions

**Choice**: Apache License 2.0
**Decided**: 2026-05-02 (Cycle 7, Phase 1 of MISSION #94)
**Status**: Provisional — revisit after Farm Hack network input

## Why Apache 2.0 (the safe-default balanced answer)

1. **Permissive + patent grant**. Apache 2.0 is MIT-class permissive but adds an explicit patent license clause that protects users from patent-troll forks. Sovereign-comms tech is exactly the kind of niche that could attract patent claims; the explicit grant is meaningful insurance.

2. **Upstream-compatible**. Reticulum, LXMF, and Sideband are all MIT-licensed. Apache 2.0 is fully compatible with MIT — we can incorporate upstream code without relicensing-friction, and we can upstream PRs back without copyleft viral concerns.

3. **Industry standard for serious permissive projects**. Android itself, most Google open-source libraries, Kubernetes, Kafka, etc. — Apache 2.0 is what major open ecosystems use when they want permissive-but-defensible.

4. **App-store friendly everywhere**. No history of Play Store / F-Droid friction. AGPL has had episodes; Apache hasn't.

5. **Zero contribution friction**. Corporate, hobbyist, farmer — anyone can contribute without legal review escalation. We want the contributor pool wide.

## What this choice trades away

- **Doesn't enforce sovereign-comms ethos in the license itself**. A BigCo could fork AugerLink, polish it, and sell as proprietary SaaS without contributing back. Apache 2.0 permits this. We're betting that the brand + community is what matters, not the legal moat.
- **No protection against extractive forks**. If someone wants to build a closed ecosystem on top, the license allows it. Mitigation: the brand name "AugerLink" remains under our control (trademark posture, not license posture).
- **Doesn't signal movement-aligned commitment as loudly as GPL/AGPL would**. For Farm Hack alliance audiences, Apache 2.0 reads as "open-source startup" rather than "commons-protector." If Farm Hack alliance contacts read this as a values mismatch, we should revisit.

## Revisit triggers

We should re-open this decision if any of these occur:

1. **Farm Hack alliance contacts explicitly request copyleft.** Their movement-network sense of authentic commons-protection trumps our pre-input default.
2. **A first major proprietary fork emerges** that's clearly extractive (e.g., a closed agtech vendor takes AugerLink, slaps their logo on it, sells as a paid app to farmers without contributing improvements back).
3. **Patent-troll incident.** If we get targeted, we want to verify Apache 2.0's patent clause holds up; if not, may need stronger.
4. **Farm Hack alliance adopts a uniform license posture** for their ecosystem and we're outside it.
5. **Phase 2 (core library extraction) reveals we want the LIBRARY under one license and the APP under another** — at that point we may want LGPL for the library + GPL for the app, or similar split.

## Mechanics of changing the license later

Apache 2.0 → MIT: trivial (more permissive direction). All contributors' code is already permissively licensed; relicensing is just a notice change.

Apache 2.0 → GPL/AGPL: requires unanimous contributor consent or rewriting any contribution that was made under Apache. Practically: we can dual-license (Apache 2.0 + GPL-3.0) without consent because Apache code is GPL-3.0 compatible, but going GPL-only requires a contributor agreement going forward. **The longer we wait to switch to copyleft, the harder it gets.**

Apache 2.0 + dual-license to GPL: feasible at any point. This is the recommended path if we want to add copyleft signaling later without losing permissive contributors.

## Decision log

| Date | Decision | Rationale |
|---|---|---|
| 2026-05-02 | Apache 2.0 provisional | Pre-Farm-Hack-input default; balanced safety with patent grant |

(Append rows as the conversation evolves.)

## References

- Apache License 2.0 text: `LICENSE` (this directory)
- Roadmap: `agent/public/roadmaps/2026-05-01_AugerLink_Sovereign_Comms_Messaging_App_curated_rebuild_from_51_testbed/roadmap.md` — Phase 1 success criteria
- Idea bank: `agent/public/ideas/001_*.json` — referenced "Farm Hack input pending" for license decision
- MISSION task: #94 (parent of #95 Phase 1)
