"""Summarize the complete phone benchmark, retaining every recognition failure."""
import argparse
import json
from pathlib import Path
import statistics


def summarize(source: Path):
    rows = []
    for line in source.read_text(encoding="utf-8-sig", errors="replace").splitlines():
        if "NotraProfileBench:" not in line:
            continue
        payload = line.split("NotraProfileBench:", 1)[1].strip()
        if payload.startswith("{"):
            rows.append(json.loads(payload))
    keys = [(row["profile"], row["case"]) for row in rows]
    if len(set(keys)) != len(keys):
        raise ValueError("Duplicate cases: use a log from exactly one benchmark run")
    profiles = {}
    for name in dict.fromkeys(row["profile"] for row in rows):
        cases = [row for row in rows if row["profile"] == name]
        durations = [row["elapsedMs"] for row in cases]
        profiles[name] = {
            "count": len(cases),
            "correct": sum(bool(row.get("namesCorrect") and row.get("amountsCorrect")) for row in cases),
            "amountsCorrect": sum(bool(row.get("amountsCorrect")) for row in cases),
            "errors": sum("error" in row for row in cases),
            "medianMs": statistics.median(durations),
            "minMs": min(durations),
            "maxMs": max(durations),
        }
    return {"profiles": profiles, "results": rows}


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("log", type=Path)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    report = summarize(args.log)
    print(json.dumps(report["profiles"], indent=2, ensure_ascii=False))
    if args.output:
        args.output.write_text(json.dumps(report, indent=2, ensure_ascii=False), encoding="utf-8")
