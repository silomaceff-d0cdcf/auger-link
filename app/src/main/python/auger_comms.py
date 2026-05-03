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
import threading
from collections import deque
from pathlib import Path

import RNS
import LXMF

_rns = None
_identity = None
_router = None
_local_destination = None
_autointerface_patched = False

# Inbox for messages delivered by LXMRouter. The delivery callback runs on
# RNS's dispatch thread; Kotlin polls get_incoming() to drain and surface
# them to the UI. Bounded so a flood of inbound traffic can't OOM the app
# if the UI poller is slow.
_inbox: deque = deque(maxlen=200)
_inbox_lock = threading.Lock()


# RNS config building blocks. AutoInterface gives us link-local peer
# discovery on platforms where multicast actually works; TCPClientInterface
# gives us a known-good unicast path to a configured peer (the testbed's
# fallback strategy on Android, where the OS sandbox restricts userspace
# IPv6 multicast egress even with WifiManager.MulticastLock held).
#
# When `tcp_targets` is provided to init(), each entry becomes a
# [[LAN TCP N]] block in the generated config. AutoInterface stays in
# the config too — harmless when its multicast send is EPERM'd, and a
# free upgrade path on hosts where multicast does work.

_CONFIG_HEADER = """\
[reticulum]
enable_transport = False
share_instance = Yes

[logging]
loglevel = 4

[interfaces]
  [[Default Interface]]
    type = AutoInterface
    enabled = Yes
"""

_TCP_BLOCK_TEMPLATE = """\
  [[LAN TCP {n}]]
    type = TCPClientInterface
    enabled = Yes
    target_host = {host}
    target_port = {port}
"""


def _build_config(tcp_targets: list[tuple[str, int]]) -> str:
    """Compose the RNS config with AutoInterface + zero-or-more TCP targets."""
    blocks = [
        _TCP_BLOCK_TEMPLATE.format(n=i, host=host, port=port)
        for i, (host, port) in enumerate(tcp_targets, start=1)
    ]
    return _CONFIG_HEADER + "\n" + "\n".join(blocks)


def _parse_targets_csv(csv: str) -> list[tuple[str, int]]:
    """Parse 'host1:port1,host2:port2' into a list of (host, int port).

    Empty / whitespace-only string → empty list. Malformed entries are
    skipped with a stderr warning rather than raising — caller should
    surface the count via the init() return so UI can confirm targets
    were accepted as configured.
    """
    out: list[tuple[str, int]] = []
    if not csv or not csv.strip():
        return out
    for raw in csv.split(","):
        item = raw.strip()
        if not item:
            continue
        if ":" not in item:
            print(f"[auger_comms] dropping malformed target {item!r} (no port)", flush=True)
            continue
        host, port_str = item.rsplit(":", 1)
        try:
            out.append((host.strip(), int(port_str.strip())))
        except ValueError:
            print(f"[auger_comms] dropping malformed target {item!r} (bad port)", flush=True)
    return out


def _install_autointerface_patch() -> None:
    """Replace AutoInterface.interface_name_to_index with a Chaquopy-friendly
    implementation that uses netinfo instead of socket.if_nametoindex.

    Idempotent — second invocation is a no-op. Must run BEFORE any
    AutoInterface is constructed (which happens during RNS.Reticulum() init
    when an [[AutoInterface]] block is present in the config).

    The replacement matches what RNS already does on Windows: query the
    netinfo helper that ships with RNS (RNS/Interfaces/util/netinfo.py)
    rather than the stdlib socket function.
    """
    global _autointerface_patched
    if _autointerface_patched:
        return

    from RNS.Interfaces import AutoInterface as AI_module

    def _patched(self, ifname):
        # First try RNS's own netinfo helper. On many Android builds this
        # returns the right index, but for some interfaces (including
        # wlan0 in some states) bionic's getifaddrs() reports None and the
        # helper drops them. The Java fallback below catches that case.
        idx = self.netinfo.interface_names_to_indexes().get(ifname)
        if idx is not None:
            return int(idx)
        # Fallback: ask Java directly. java.net.NetworkInterface.getByName
        # is the source-of-truth on Android and returns the kernel interface
        # index even for interfaces bionic chokes on.
        from java.net import NetworkInterface
        iface = NetworkInterface.getByName(ifname)
        if iface is None:
            raise OSError(f"interface {ifname!r} not found via NetworkInterface.getByName")
        return int(iface.getIndex())

    AI_module.AutoInterface.interface_name_to_index = _patched
    _autointerface_patched = True
    print("[auger_comms] installed AutoInterface caller-provides-indexes patch", flush=True)


def _on_lxm_delivered(message) -> None:
    """LXMRouter delivery callback. Runs on RNS's dispatch thread.

    Must NOT raise — uncaught exceptions break RNS's dispatch loop and
    silently kill all subsequent inbound deliveries. The whole body is
    wrapped in a broad except so the worst case is a logged warning, not
    a dispatch-thread death.

    Snapshot dict format (what Kotlin sees):
      source_hash:     hex string (16 bytes), sender's LXMF dest hash
      title:           UTF-8 string (may be empty)
      content:         UTF-8 string (the message body)
      timestamp:       float seconds since epoch (sender-claimed)
      signature_valid: bool — RNS's signature verification result
    """
    try:
        snapshot = {
            "source_hash": message.source_hash.hex() if getattr(message, "source_hash", None) else "",
            "title": (message.title_as_string() or "").strip(),
            "content": (message.content_as_string() or "").strip(),
            "timestamp": float(message.timestamp) if getattr(message, "timestamp", None) else 0.0,
            "signature_valid": bool(getattr(message, "signature_validated", False)),
        }
    except Exception as e:
        # Don't let a malformed message kill the dispatch thread.
        snapshot = {
            "source_hash": "",
            "title": "",
            "content": "",
            "timestamp": 0.0,
            "signature_valid": False,
            "_error": f"snapshot failed: {type(e).__name__}: {e}",
        }

    with _inbox_lock:
        _inbox.append(snapshot)
    print(
        f"[auger_comms] inbound queued — content_len={len(snapshot.get('content', ''))} "
        f"sig_ok={snapshot.get('signature_valid', False)}",
        flush=True,
    )


def get_incoming() -> list:
    """Drain and return queued inbound messages. Kotlin polls this.

    Each call returns messages received since the last call. Empty list
    if no new messages.
    """
    with _inbox_lock:
        out = list(_inbox)
        _inbox.clear()
    return out


def init(files_dir: str, tcp_targets_csv: str = "") -> dict:
    """Initialize Reticulum + LXMF. Idempotent.

    Args:
        files_dir: absolute path to the app's private filesystem area
            (Android Application.getFilesDir().getAbsolutePath()).
        tcp_targets_csv: optional comma-separated list of "host:port"
            entries to add as TCPClientInterface blocks alongside
            AutoInterface. Empty string (default) → AutoInterface-only
            config. The expected user flow is to set this from a
            Settings screen once we surface that UI.

    Returns:
        dict with keys:
            ok (bool): True on success, False on error
            lxmf_dest (str): 32-char hex destination hash (when ok)
            tcp_targets (int): number of TCP targets accepted into config
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

    tcp_targets = _parse_targets_csv(tcp_targets_csv)

    saved_handler = signal.getsignal(signal.SIGINT)

    try:
        # Install the AutoInterface patch BEFORE Reticulum() is constructed.
        _install_autointerface_patch()


        files = Path(files_dir)
        rns_cfg = files / "rns_config"
        rns_cfg.mkdir(parents=True, exist_ok=True)

        # Always rewrite the config — guarantees we get the desired
        # interface set even on subsequent launches (the user may have
        # changed TCP targets in Settings since last run).
        cfg_path = rns_cfg / "config"
        cfg_path.write_text(_build_config(tcp_targets))
        print(
            f"[auger_comms.init] wrote config to {cfg_path} "
            f"(AutoInterface + {len(tcp_targets)} TCP target(s))",
            flush=True,
        )

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
        _router.register_delivery_callback(_on_lxm_delivered)

        _local_destination = _router.register_delivery_identity(
            _identity, display_name="AugerLink"
        )
        _local_destination.announce()

        dest_hex = _local_destination.hash.hex()
        print(
            f"[auger_comms.init] OK — lxmf_dest={dest_hex}, "
            f"tcp_targets={len(tcp_targets)}",
            flush=True,
        )
        return {"ok": True, "lxmf_dest": dest_hex, "tcp_targets": len(tcp_targets)}

    except Exception as e:
        print(f"[auger_comms.init] FAILED: {type(e).__name__}: {e}", flush=True)
        return {"ok": False, "error": f"{type(e).__name__}: {e}"}
    finally:
        signal.signal(signal.SIGINT, saved_handler)


def send_message(dest_hash_hex: str, content: str, title: str = "") -> dict:
    """Send an LXMF text message to a destination identified by hex hash.

    Args:
        dest_hash_hex: 32-char hex destination hash of the recipient's
            LXMF delivery identity.
        content: message body (UTF-8 string).
        title: optional message title (LXMF supports a separate title
            field; default empty).

    Returns:
        dict with keys:
            ok (bool): True if outbound was queued for delivery
            lxm_hash (str): hex hash of the LXMF message (when ok)
            error (str): error message (when not ok)

    Notes:
        - "ok=True" means LXMRouter accepted the outbound for delivery,
          NOT that the recipient has received it. Delivery is async and
          surfaces via the receive-callback wiring (next microcommit).
        - If the destination's path is not known to RNS yet, this returns
          ok=False with a "path not known" error and triggers a path
          request in the background. Caller should retry in a few seconds.
    """
    if _router is None or _identity is None or _local_destination is None:
        return {"ok": False, "error": "router not initialized — call init() first"}

    try:
        dest_bytes = bytes.fromhex(dest_hash_hex.strip().lower())
    except Exception as e:
        return {"ok": False, "error": f"bad dest hash: {e}"}

    try:
        dest_identity = RNS.Identity.recall(dest_bytes)
        if dest_identity is None:
            RNS.Transport.request_path(dest_bytes)
            return {"ok": False, "error": "path not known yet — retry in a few seconds"}

        dest = RNS.Destination(
            dest_identity,
            RNS.Destination.OUT,
            RNS.Destination.SINGLE,
            "lxmf",
            "delivery",
        )
    except Exception as e:
        return {"ok": False, "error": f"destination resolve failed: {e}"}

    try:
        lxm = LXMF.LXMessage(
            destination=dest,
            source=_local_destination,
            content=content,
            title=title,
            desired_method=LXMF.LXMessage.DIRECT,
        )
        _router.handle_outbound(lxm)
    except Exception as e:
        return {"ok": False, "error": f"handle_outbound failed: {e}"}

    lxm_hash = lxm.hash.hex() if hasattr(lxm, "hash") else None
    # Don't log dest hash or content — both are identifying. Length only.
    print(
        f"[auger_comms.send_message] queued — len={len(content)} lxm_hash_present={lxm_hash is not None}",
        flush=True,
    )
    return {"ok": True, "lxm_hash": lxm_hash}
