"""Порівнює пікову пам'ять і час: список записів проти потокової обробки."""

from __future__ import annotations

import random
import sys
import time
import tracemalloc
from itertools import islice
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))

from kzp_lab06.analysis import make_summary, stream_summary  # noqa: E402
from kzp_lab06.loader import iter_records, load_records  # noqa: E402
from kzp_lab06.reporting import format_report  # noqa: E402

ROWS = 200_000


def generate(path: Path, rows: int = ROWS) -> None:
    """Створює великий CSV (детерміновано, зерно 6)."""
    rng = random.Random(6)
    with path.open("w", encoding="utf-8", newline="") as target:
        target.write("full_name,group,year,average\n")
        for number in range(rows):
            target.write(
                f"Студент {number},КІ-{300 + number % 9},{1 + number % 6},"
                f"{rng.uniform(40, 100):.1f}\n"
            )


def measure(label: str, action) -> dict[str, object]:
    """Вимірює пікову пам'ять (tracemalloc) і час однієї дії."""
    tracemalloc.start()
    started = time.perf_counter()
    summary = action()
    elapsed = time.perf_counter() - started
    _, peak = tracemalloc.get_traced_memory()
    tracemalloc.stop()
    print(f"{label}: пік {peak / 1024 / 1024:.2f} МіБ, {elapsed:.2f} с")
    return summary


def main() -> int:
    """Запускає порівняння та перевіряє, що результати однакові."""
    # Явний UTF-8 для консолі: типове кодування Windows не знає українських літер.
    for stream in (sys.stdout, sys.stderr):
        stream.reconfigure(encoding="utf-8")
    path = ROOT / "data" / "big.csv"
    generate(path)
    first_rows = list(islice(iter_records(path), 3))  # itertools.islice: лише 3 записи
    print(f"Перші записи без читання всього файла: {len(first_rows)}")
    by_list = measure("список записів", lambda: make_summary(load_records(path)))
    by_stream = measure("потоковий режим", lambda: stream_summary(iter_records(path)))
    same = format_report(by_list) == format_report(by_stream)
    print("Звіти однакові:", same)
    path.unlink()
    return 0 if same else 1


if __name__ == "__main__":
    sys.exit(main())
