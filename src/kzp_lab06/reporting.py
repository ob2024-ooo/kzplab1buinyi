"""Формування й запис текстового звіту. Модуль не читає CSV."""

from __future__ import annotations

from pathlib import Path
from typing import Any


def format_report(summary: dict[str, Any]) -> str:
    """Форматує підсумок у детермінований текстовий звіт.

    Порядок рядків фіксований, групи відсортовано, числа мають два знаки.
    """
    lines = [
        "Реєстр студентів: звіт",
        f"Записів: {summary['count']}",
        f"Груп: {len(summary['groups'])}",
        f"Відмінників (середній бал >= {summary['threshold']:.1f}): "
        f"{summary['excellent_count']}",
    ]
    by_group: dict[str, int] = summary["excellent_by_group"]
    if by_group:
        lines.append("Відмінники за групами:")
        lines.extend(f"{group}: {by_group[group]}" for group in sorted(by_group))
    lines.append(f"Середній бал: {summary['average']:.2f}")
    best = summary["best"]
    if best is None:
        lines.append("Найкращий результат: немає даних")
    else:
        lines.append(
            f"Найкращий результат: {best['full_name']} ({best['group']}), "
            f"{best['average']:.2f}"
        )
    return "\n".join(lines) + "\n"


def write_report(path: Path, text: str) -> None:
    """Записує звіт у UTF-8, створюючи каталог призначення за потреби."""
    path.parent.mkdir(parents=True, exist_ok=True)
    # with гарантує закриття файла навіть у разі помилки запису.
    with path.open("w", encoding="utf-8", newline="\n") as target:
        target.write(text)
