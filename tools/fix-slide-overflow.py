#!/usr/bin/env python3
"""Verkleinert ueberlaufende Marp-Folien ueber Dichte-Klassen.

    python3 tools/fix-slide-overflow.py slides/00_Spring_Core/slides.md
    python3 tools/fix-slide-overflow.py --dry-run slides/*/slides.md
    python3 tools/fix-slide-overflow.py --reset slides/00_Spring_Core/slides.md

Setzt zuerst alle eigenen Marken zurueck, misst mit
tools/check-slide-overflow.mjs, weist jeder ueberlaufenden Folie die
groesste noch ausreichende Stufe zu und misst nach. Bleibt etwas offen,
wird die naechstkleinere Stufe versucht.

Der Text der Folien wird nicht angefasst. Eingefuegt werden nur ein
<style>-Block hinter dem Front-Matter und je eine Kommentarzeile
<!-- _class: ... --> vor dem Inhalt der betroffenen Folie. Gearbeitet wird
auf Zeilenindizes, es wird also nur eingefuegt und geloescht.

Grenze der Methode: font-size skaliert keine Bilder. Eine Folie, die wegen
einer Grafik ueberlaeuft, laesst sich so nicht retten und wird gemeldet.
"""
import argparse
import json
import re
import subprocess
import sys
from pathlib import Path

# Anteil der Grundschriftgroesse je Stufe, absteigend.
TIER_FACTORS = [("dense", 0.85), ("denser", 0.72), ("densest", 0.60), ("densest-xs", 0.53)]
TIER_NAMES = [t[0] for t in TIER_FACTORS]

MARKER = "<!-- Dichte-Stufen gegen Folienueberlauf, siehe tools/check-slide-overflow.mjs -->"
CLASS_RE = re.compile(r"^\s*<!--\s*_class:\s*(?:" + "|".join(TIER_NAMES) + r")\s*-->\s*$")

TOLERANCE = 2
MAX_ROUNDS = 5
REPO = Path(__file__).resolve().parent.parent


def measure(files):
    out = subprocess.run(
        ["node", str(REPO / "tools/check-slide-overflow.mjs"), "--json", *files],
        capture_output=True, text=True, cwd=REPO, stdin=subprocess.DEVNULL,
    )
    if not out.stdout.strip():
        sys.exit("Messung lieferte kein Ergebnis:\n" + out.stderr.strip()[-2000:])
    return json.loads(out.stdout)


def frontmatter_end(lines):
    if not lines or lines[0].strip() != "---":
        raise SystemExit("Kein Front-Matter gefunden, ist das ein Marp-Deck?")
    return next(i for i in range(1, len(lines)) if lines[i].strip() == "---")


def slide_starts(lines, fm_end):
    """Zeilenindex, an dem der Inhalt jeder Folie beginnt. Trenner innerhalb
    von Codebloecken zaehlen nicht als Folienwechsel."""
    starts, fence, pending = [], None, True
    for i in range(fm_end + 1, len(lines)):
        m = re.match(r"^\s*(`{3,}|~{3,})", lines[i])
        if m:
            tok = m.group(1)[0]
            fence = None if fence == tok else (fence or tok)
        if fence is None and lines[i].strip() == "---":
            pending = True
            continue
        if pending and lines[i].strip():
            starts.append(i)
            pending = False
    return starts


def strip_existing(lines):
    """Entfernt Style-Block und Marken frueherer Laeufe."""
    out, i = [], 0
    while i < len(lines):
        if lines[i].strip() == MARKER:
            i += 1
            while i < len(lines) and "</style>" not in lines[i]:
                i += 1
            i += 1
            continue
        if CLASS_RE.match(lines[i]):
            i += 1
            continue
        out.append(lines[i])
        i += 1
    return out


def normalize_gap(lines, after):
    """Genau eine Leerzeile hinter Index `after`."""
    j = after + 1
    while j < len(lines) and not lines[j].strip():
        del lines[j]
    lines.insert(j, "")


def write(path, assigns, base_font):
    lines = strip_existing(path.read_text(encoding="utf-8").split("\n"))
    end = frontmatter_end(lines)
    if assigns:
        starts = slide_starts(lines, end)
        for n in sorted(assigns, reverse=True):
            lines.insert(starts[n - 1], f"<!-- _class: {assigns[n]} -->")
        used = [t for t in TIER_NAMES if t in set(assigns.values())]
        px = {n: round(f * base_font * 2) / 2 for n, f in TIER_FACTORS}
        block = [MARKER, "<style>"]
        block += [f"section.{t} {{ font-size: {px[t]:g}px; }}" for t in used]
        block += ["</style>"]
        end = frontmatter_end(lines)
        lines[end + 1 : end + 1] = block
        end += len(block)
    normalize_gap(lines, end)
    path.write_text("\n".join(lines), encoding="utf-8")


def smallest_sufficient(row, floor=0):
    """Groesste Stufe ab floor, die den Inhalt in die Box bringt."""
    inner = row["box"] - row["pad"]
    needed = inner / (row["content"] - row["pad"])
    for i, (name, factor) in enumerate(TIER_FACTORS):
        if i >= floor and factor <= needed * 0.98:
            return name
    return TIER_NAMES[-1]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("files", nargs="+")
    ap.add_argument("--reset", action="store_true", help="nur eigene Marken entfernen")
    ap.add_argument("--dry-run", action="store_true", help="Zuweisung zeigen, nichts schreiben")
    args = ap.parse_args()
    paths = {f: Path(f) for f in args.files}
    # --dry-run laeuft vollstaendig durch und stellt am Ende wieder her.
    # Nur so entspricht die gemeldete Zuweisung dem, was ein echter Lauf
    # ergaebe; ein Abbruch nach der ersten Runde zeigte Zwischenstaende.
    original = {f: p.read_text(encoding="utf-8") for f, p in paths.items()}
    try:
        return run(args, paths)
    finally:
        if args.dry_run:
            for f, p in paths.items():
                p.write_text(original[f], encoding="utf-8")
            print("(--dry-run: Dateien unveraendert zurueckgesetzt)")


def run(args, paths):
    for p in paths.values():
        write(p, {}, 0)
    if args.reset:
        print("Marken entfernt: " + ", ".join(args.files))
        return 0

    data = measure(args.files)
    base = {f: max(r["font"] for r in data[f]) for f in args.files}
    assigns = {f: {} for f in args.files}
    stuck = []

    for rnd in range(1, MAX_ROUNDS + 1):
        todo = False
        for f in args.files:
            for row in data[f]:
                if row["over"] <= TOLERANCE:
                    continue
                n = row["slide"]
                cur = assigns[f].get(n)
                floor = TIER_NAMES.index(cur) + 1 if cur else 0
                if floor >= len(TIER_NAMES):
                    stuck.append((f, n, row["over"]))
                    continue
                assigns[f][n] = smallest_sufficient(row, floor)
                todo = True
        if not todo:
            break
        for f in args.files:
            write(paths[f], assigns[f], base[f])
        data = measure(args.files)
        stuck = []

    for f in args.files:
        marked = assigns[f]
        counts = {t: sum(1 for v in marked.values() if v == t) for t in TIER_NAMES}
        summary = ", ".join(f"{t} {counts[t]}" for t in TIER_NAMES if counts[t])
        print(f"{f}: {len(marked)} von {len(data[f])} Folien markiert" + (f" ({summary})" if summary else ""))
        tiny = sorted(n for n, t in marked.items() if t == TIER_NAMES[-1])
        if tiny:
            pct = round(TIER_FACTORS[-1][1] * 100)
            print(f"  Folien {tiny} stehen auf {pct}% der Grundgroesse. Inhaltlich besser aufteilen.")

    for f, n, over in stuck:
        print(f"  ! {f} Folie {n}: laeuft auch auf der kleinsten Stufe um {over}px ueber."
              f" Vermutlich ein Bild, das font-size nicht skaliert.")

    return 1 if stuck else 0


if __name__ == "__main__":
    sys.exit(main())
