"""Командний рядок: читає CSV, будує звіт і записує його у файл."""

from __future__ import annotations

import argparse
import sys
from collections.abc import Sequence
from pathlib import Path

from kzp_lab07.analysis import THRESHOLD, make_summary
from kzp_lab07.errors import DataError
from kzp_lab07.loader import load_learners
from kzp_lab07.reporting import format_report, write_report

EXIT_OK = 0
EXIT_DATA_ERROR = 1
EXIT_FILE_ERROR = 3
EXIT_UNEXPECTED = 4


def build_parser() -> argparse.ArgumentParser:
    """Створює розбірник аргументів (помилки використання дають код 2)."""
    parser = argparse.ArgumentParser(
        prog="kzp-lab07", description="Звіт за CSV реєстру студентів (варіант 6)."
    )
    parser.add_argument("input", type=Path, help="вхідний CSV (UTF-8)")
    parser.add_argument("output", type=Path, help="файл звіту")
    parser.add_argument(
        "--threshold",
        type=float,
        default=THRESHOLD,
        help="нижня межа середнього балу відмінника (за замовчуванням 90)",
    )
    parser.add_argument(
        "--ratings",
        action="store_true",
        help="додати до звіту рейтинг груп (Bachelor і Master мають різні ваги)",
    )
    return parser


def main(argv: Sequence[str] | None = None) -> int:
    """Точка входу. Повертає код завершення: 0, 1 (дані), 3 (файл), 4 (інше)."""
    # Явний UTF-8 для консолі: типове кодування Windows може бути іншим.
    for stream in (sys.stdout, sys.stderr):
        if hasattr(stream, "reconfigure"):
            stream.reconfigure(encoding="utf-8")
    args = build_parser().parse_args(argv)
    try:
        summary = make_summary(load_learners(args.input), args.threshold)
        write_report(args.output, format_report(summary, args.ratings))
    except DataError as error:
        print(f"Помилка даних: {error}", file=sys.stderr)
        if error.__cause__ is not None:
            print(f"Причина: {error.__cause__}", file=sys.stderr)
        return EXIT_DATA_ERROR
    except OSError as error:
        print(f"Помилка файла: {error}", file=sys.stderr)
        return EXIT_FILE_ERROR
    except Exception as error:  # noqa: BLE001 - межа CLI: не маскуємо, а повідомляємо
        print(f"Неочікувана помилка: {error!r}", file=sys.stderr)
        return EXIT_UNEXPECTED
    print(f"Звіт записано: {args.output}")
    return EXIT_OK


if __name__ == "__main__":
    raise SystemExit(main())
