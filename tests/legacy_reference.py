"""Еталонні словникові обчислення (логіка лабораторної №6) для порівняння."""

import csv
from pathlib import Path


def load_dicts(path: Path) -> list[dict]:
    """Читає CSV у словники з типізованими полями (стара модель)."""
    with path.open("r", encoding="utf-8", newline="") as source:
        return [
            {
                "full_name": " ".join(row["full_name"].split()),
                "group": " ".join(row["group"].split()),
                "year": int(row["year"]),
                "average": float(row["average"]),
            }
            for row in csv.DictReader(source)
        ]


def legacy_summary(records: list[dict], threshold: float = 90.0) -> dict:
    """Підсумок за словниками: кількість, групи, відмінники, середній бал, найкращий."""
    excellent = [r for r in records if r["average"] >= threshold]
    groups = {r["group"] for r in records}
    best = min(
        records, key=lambda r: (-r["average"], r["full_name"], r["group"]), default=None
    )
    return {
        "count": len(records),
        "groups": groups,
        "excellent_count": len(excellent),
        "excellent_by_group": {
            g: sum(1 for r in excellent if r["group"] == g)
            for g in sorted({r["group"] for r in excellent})
        },
        "average": sum(r["average"] for r in records) / len(records)
        if records
        else 0.0,
        "best": best,
    }
