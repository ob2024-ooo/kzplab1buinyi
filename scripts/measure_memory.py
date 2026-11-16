"""Порівнює пам'ять і час: словникова та об'єктна моделі на однакових даних."""

from __future__ import annotations

import sys
import time
import tracemalloc
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from kzp_lab07.models import Bachelor, Master  # noqa: E402

ROWS = 100_000


def make_dicts() -> list[dict]:
    """Створює записи як словники (модель лабораторної №6)."""
    return [
        {
            "full_name": f"Студент {i}",
            "group": f"КІ-{300 + i % 9}",
            "year": 1 + i % 6,
            "average": 40 + (i % 600) / 10,
        }
        for i in range(ROWS)
    ]


def make_objects() -> list:
    """Створює ті самі записи як об'єкти Bachelor і Master."""
    result = []
    for i in range(ROWS):
        year = 1 + i % 6
        kind = Bachelor if year <= 4 else Master
        result.append(
            kind(f"Студент {i}", f"КІ-{300 + i % 9}", year, 40 + (i % 600) / 10)
        )
    return result


def measure(label: str, action) -> None:
    """Вимірює пік пам'яті (tracemalloc) і час створення записів."""
    tracemalloc.start()
    started = time.perf_counter()
    data = action()
    elapsed = time.perf_counter() - started
    current, peak = tracemalloc.get_traced_memory()
    tracemalloc.stop()
    print(
        f"{label}: {len(data)} записів, у пам'яті {current / 1024 / 1024:.2f} МіБ, "
        f"пік {peak / 1024 / 1024:.2f} МіБ, {elapsed:.2f} с"
    )


def main() -> int:
    """Запускає порівняння."""
    for stream in (sys.stdout, sys.stderr):
        stream.reconfigure(encoding="utf-8")
    measure("словники", make_dicts)
    measure("об'єкти ", make_objects)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
