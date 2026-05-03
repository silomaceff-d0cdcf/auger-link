"""AugerLink communications init — minimal Phase 2 step.

Exposes a top-level init(files_dir) that creates a Reticulum router +
LXMF identity + LXMRouter and returns the destination hash. Idempotent —
subsequent calls return the existing destination without re-initializing.

This is the smallest possible bridge that proves Chaquopy + RNS + LXMF
work end-to-end on Android. Subsequent microcommits add:
  - send_message(dest_hex, content) — outbound
  - receive callback wiring + queue surface — inbound
  - AutoInterface caller-provides-indexes monkey-patch for peer discovery

Architecture notes:
  - RNS.Reticulum() and LXMF.LXMRouter() both install SIGINT handlers on
    construction. We stash and restore SIGINT around init so Android's
    own signal routing isn't disturbed.
  - All long-lived state (Reticulum, Identity, LXMRouter, local
    destination) is stashed at module level — Chaquopy preserves Python
    module state across the JVM <-> Python boundary, so re-importing
    the module from Kotlin returns the same singletons.
  - The configdir, identity file, and LXMF storage all live under the
    caller-provided files_dir (Android Application.getFilesDir()). RNS
    creates a minimal config.toml on first run; no interfaces declared
    yet (auto-discovery comes with the AutoInterface monkey-patch).
"""

from __future__ import annotations

import signal
from pathlib import Path

import RNS
import LXMF

_rns = None
_identity = None
_router = None
_local_destination = None


# Reticulum's default config enables AutoInterface, which calls
# socket.if_nametoindex(). Chaquopy's bundled Python doesn't ship that symbol
# (stripped from python-for-android's stdlib for size), so AutoInterface
# crashes on construction. Until the AutoInterface caller-provides-indexes
# monkey-patch lands in a later microcommit, we ship a minimal config with
# NO interfaces — Reticulum still boots, the local Identity + LXMRouter
# still create cleanly, but there's no transport. Outbound delivery comes
# online when interfaces are configured (TCP targets and/or patched
# AutoInterface in subsequent steps).
_MINIMAL_CONFIG = """\
[reticulum]
enable_transport = False
share_instance = Yes

[logging]
loglevel = 4

[interfaces]
"""


def init(files_dir: str) -> dict:
    """Initialize Reticulum + LXMF. Idempotent.

    Args:
        files_dir: absolute path to the app's private filesystem area
            (Android Application.getFilesDir().getAbsolutePath()).

    Returns:
        dict with keys:
            ok (bool): True on success, False on error
            lxmf_dest (str): 32-char hex destination hash (when ok)
            error (str): error message (when not ok)
            note (str): "already initialized" if called twice (when ok)
    """
    global _rns, _identity, _router, _local_destination

    if _router is not None and _local_destination is not None:
        return {
            "ok": True,
            "lxmf_dest": _local_destination.hash.hex(),
            "note": "already initialized",
        }

    saved_handler = signal.getsignal(signal.SIGINT)

    try:
        files = Path(files_dir)
        rns_cfg = files / "rns_config"
        rns_cfg.mkdir(parents=True, exist_ok=True)

        # Always rewrite the config — guarantees we get the no-interface
        # configuration even on subsequent launches (Reticulum's first-run
        # auto-generated config enables AutoInterface and would crash here).
        cfg_path = rns_cfg / "config"
        cfg_path.write_text(_MINIMAL_CONFIG)
        print(f"[auger_comms.init] wrote minimal config to {cfg_path}", flush=True)

        print(f"[auger_comms.init] RNS.Reticulum(configdir={rns_cfg})", flush=True)
        _rns = RNS.Reticulum(configdir=str(rns_cfg))

        identity_file = files / "auger_identity"
        if identity_file.exists():
            print(f"[auger_comms.init] loading identity from {identity_file}", flush=True)
            _identity = RNS.Identity.from_file(str(identity_file))
        else:
            print(f"[auger_comms.init] generating new identity at {identity_file}", flush=True)
            _identity = RNS.Identity()
            _identity.to_file(str(identity_file))

        storage = files / "lxmf_storage"
        storage.mkdir(parents=True, exist_ok=True)
        print(f"[auger_comms.init] LXMRouter(storagepath={storage})", flush=True)
        _router = LXMF.LXMRouter(identity=_identity, storagepath=str(storage))

        _local_destination = _router.register_delivery_identity(
            _identity, display_name="AugerLink"
        )
        _local_destination.announce()

        dest_hex = _local_destination.hash.hex()
        print(f"[auger_comms.init] OK — lxmf_dest={dest_hex}", flush=True)
        return {"ok": True, "lxmf_dest": dest_hex}

    except Exception as e:
        print(f"[auger_comms.init] FAILED: {type(e).__name__}: {e}", flush=True)
        return {"ok": False, "error": f"{type(e).__name__}: {e}"}
    finally:
        signal.signal(signal.SIGINT, saved_handler)
