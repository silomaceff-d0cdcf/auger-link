#!/usr/bin/env python3
"""WCAG 2.1 contrast-ratio verifier for the AugerLink palette variants.

Two variants are defined:

  - night-shift  (default) — harvest-warm palette pulled from the hero concept art.
  - day-shift              — cool-mute variant for less seasonal-coded contexts.

Usage:
    python3 design/wcag_verify.py                                # night-shift, markdown
    python3 design/wcag_verify.py --variant day-shift            # day-shift, markdown
    python3 design/wcag_verify.py --variant day-shift --json     # machine-readable
    python3 design/wcag_verify.py --variant day-shift --strict   # exit 1 on AA failure

The 10 fg/bg pairs verified per variant are the load-bearing combinations the
palette commits to. Both variants must pass AA on all 10. Source of truth for
the tokens is `app/src/main/kotlin/io/silomaceff/augerlink/ui/theme/Color.kt`;
this script duplicates them locally so verification is self-contained.

WCAG 2.1 contrast formula:
    L = 0.2126*R + 0.7152*G + 0.0722*B   (each component sRGB-linearized)
    ratio = (L_lighter + 0.05) / (L_darker + 0.05)

References:
- https://www.w3.org/TR/WCAG21/#contrast-minimum
- https://www.w3.org/WAI/GL/wiki/Relative_luminance
"""

from __future__ import annotations
import argparse
import json
import sys
from dataclasses import dataclass


def _linearize(c8: int) -> float:
    s = c8 / 255.0
    return s / 12.92 if s <= 0.03928 else ((s + 0.055) / 1.055) ** 2.4


def relative_luminance(hex_color: str) -> float:
    h = hex_color.lstrip("#")
    if len(h) != 6:
        raise ValueError(f"expected 6-digit hex, got {hex_color!r}")
    r, g, b = (int(h[i : i + 2], 16) for i in (0, 2, 4))
    return 0.2126 * _linearize(r) + 0.7152 * _linearize(g) + 0.0722 * _linearize(b)


def contrast_ratio(fg: str, bg: str) -> float:
    l1 = relative_luminance(fg)
    l2 = relative_luminance(bg)
    lighter, darker = (l1, l2) if l1 >= l2 else (l2, l1)
    return (lighter + 0.05) / (darker + 0.05)


@dataclass(frozen=True)
class Pair:
    fg_token: str
    fg_hex: str
    bg_token: str
    bg_hex: str
    min_ratio: float
    use_case: str


# Palette definitions — kept in sync with Color.kt manually (Phase 1 simplicity;
# Phase 2 may extract a shared JSON/TOML source-of-truth).
NIGHT_SHIFT_PAIRS: list[Pair] = [
    Pair("cream",        "#F5E6D3", "barn-dark",   "#1A1410", 4.5, "Body text on app bg"),
    Pair("cream",        "#F5E6D3", "charcoal",    "#0F0E0C", 4.5, "Text on cards/bubbles"),
    Pair("cream-muted",  "#C8B8A0", "barn-dark",   "#1A1410", 4.5, "Secondary text on app bg"),
    Pair("dusk-amber",   "#9C5519", "barn-dark",   "#1A1410", 3.0, "Primary button bg vs app bg"),
    Pair("cream",        "#F5E6D3", "dusk-amber",  "#9C5519", 4.5, "Button label on primary"),
    Pair("glow-orange",  "#D97826", "barn-dark",   "#1A1410", 3.0, "Outgoing bubble vs app bg"),
    Pair("barn-dark",    "#1A1410", "glow-orange", "#D97826", 4.5, "Outgoing bubble text"),
    Pair("gold",         "#FFC065", "barn-dark",   "#1A1410", 3.0, "Active indicator on app bg"),
    Pair("error",        "#E53E2C", "barn-dark",   "#1A1410", 3.0, "Error text/icon on app bg"),
    Pair("success",      "#7FA756", "barn-dark",   "#1A1410", 3.0, "Success text/icon on app bg"),
]

DAY_SHIFT_PAIRS: list[Pair] = [
    Pair("linen",            "#E0DCD2", "bg-dark",        "#15181B", 4.5, "Body text on app bg"),
    Pair("linen",            "#E0DCD2", "surface",        "#0E1114", 4.5, "Text on cards/bubbles"),
    Pair("taupe",            "#A8A498", "bg-dark",        "#15181B", 4.5, "Secondary text on app bg"),
    Pair("bronze",           "#9C7A52", "bg-dark",        "#15181B", 3.0, "Primary button bg vs app bg"),
    Pair("bg-dark",          "#15181B", "bronze",         "#9C7A52", 4.5, "Button label on primary"),
    Pair("oak-tan",          "#B5896A", "bg-dark",        "#15181B", 3.0, "Outgoing bubble vs app bg"),
    Pair("bg-dark",          "#15181B", "oak-tan",        "#B5896A", 4.5, "Outgoing bubble text"),
    Pair("straw-tan",        "#C4A77D", "bg-dark",        "#15181B", 3.0, "Active indicator on app bg"),
    Pair("coral",            "#D85A4F", "bg-dark",        "#15181B", 3.0, "Error text/icon on app bg"),
    Pair("sage",             "#86A88A", "bg-dark",        "#15181B", 3.0, "Success text/icon on app bg"),
]

VARIANTS = {
    "night-shift": NIGHT_SHIFT_PAIRS,
    "day-shift": DAY_SHIFT_PAIRS,
}


def aaa_min(min_aa: float) -> float:
    return 7.0 if min_aa == 4.5 else 4.5


def evaluate(pair: Pair) -> dict:
    ratio = contrast_ratio(pair.fg_hex, pair.bg_hex)
    aa = ratio >= pair.min_ratio
    aaa = ratio >= aaa_min(pair.min_ratio)
    return {
        "fg_token": pair.fg_token,
        "fg_hex": pair.fg_hex,
        "bg_token": pair.bg_token,
        "bg_hex": pair.bg_hex,
        "ratio": round(ratio, 2),
        "min_aa": pair.min_ratio,
        "min_aaa": aaa_min(pair.min_ratio),
        "passes_aa": aa,
        "passes_aaa": aaa,
        "use_case": pair.use_case,
    }


def render_table(results: list[dict]) -> str:
    rows = [
        "| Foreground | Background | Ratio | AA | AAA | Use Case |",
        "|---|---|---|---|---|---|",
    ]
    for r in results:
        aa = "✅" if r["passes_aa"] else "❌"
        aaa = "✅" if r["passes_aaa"] else "—"
        rows.append(
            f"| `{r['fg_token']}` `{r['fg_hex']}` "
            f"| `{r['bg_token']}` `{r['bg_hex']}` "
            f"| **{r['ratio']:.2f}:1** (need {r['min_aa']:.0f}:1) "
            f"| {aa} | {aaa} | {r['use_case']} |"
        )
    return "\n".join(rows)


def main(argv: list[str]) -> int:
    p = argparse.ArgumentParser(description=__doc__.split("\n", 1)[0])
    p.add_argument("--variant", choices=list(VARIANTS), default="night-shift")
    p.add_argument("--json", action="store_true", help="emit JSON instead of markdown table")
    p.add_argument("--strict", action="store_true", help="exit 1 if any pair fails AA")
    args = p.parse_args(argv)

    pairs = VARIANTS[args.variant]
    results = [evaluate(pair) for pair in pairs]

    if args.json:
        print(json.dumps({"variant": args.variant, "results": results}, indent=2))
    else:
        print(f"# AugerLink Palette — WCAG 2.1 Contrast Verification ({args.variant})\n")
        print(f"Generated by `design/wcag_verify.py --variant {args.variant}` (deterministic).\n")
        print(render_table(results))
        n_fail = sum(1 for r in results if not r["passes_aa"])
        n_aaa = sum(1 for r in results if r["passes_aaa"])
        print(f"\n**Summary**: {len(results) - n_fail}/{len(results)} pairs pass AA; "
              f"{n_aaa}/{len(results)} pairs also pass AAA.")
        if n_fail:
            print(f"\n⚠️  {n_fail} pair(s) FAIL AA — adjust foreground value (HSV V) toward brighter end "
                  "while keeping hue + warm character per palette.md guidance.")

    if args.strict and any(not r["passes_aa"] for r in results):
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
