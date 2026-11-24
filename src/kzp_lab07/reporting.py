"""Формування й запис текстового звіту. Модуль не читає CSV."""

from __future__ import annotations

import contextlib
from pathlib import Path
from types import TracebackType
from typing import TextIO

from kzp_lab07.models import LearnerSummary


def format_report(summary: LearnerSummary, with_ratings: bool = False) -> str:
    """Форматує підсумок у детермінований текстовий звіт.

    Основний текст збігається зі звітом лабораторної №6. Розділ рейтингів груп
    додається лише на вимогу (``with_ratings``).
    """
    lines = [
        "Реєстр студентів: звіт",
        f"Записів: {summary.count}",
        f"Груп: {len(summary.groups)}",
        f"Відмінників (середній бал >= {summary.threshold:.1f}): "
        f"{summary.excellent_count}",
    ]
    if summary.excellent_by_group:
        lines.append("Відмінники за групами:")
        lines.extend(f"{group}: {count}" for group, count in summary.excellent_by_group)
    lines.append(f"Середній бал: {summary.average:.2f}")
    if summary.best_name is None:
        lines.append("Найкращий результат: немає даних")
    else:
        lines.append(
            f"Найкращий результат: {summary.best_name} ({summary.best_group}), "
            f"{summary.best_average:.2f}"
        )
    if with_ratings:
        lines.append("Рейтинг груп:")
        lines.extend(
            f"{item.group}: {item.average_rating:.2f} ({item.count})"
            for item in summary.group_ratings
        )
    return "\n".join(lines) + "\n"


class ReportWriter:
    """Контекстний менеджер безпечного запису звіту.

    Текст пишеться у тимчасовий файл поряд із цільовим. Після успішного
    блоку файл замінює цільовий; якщо всередині блоку виник виняток, тимчасовий
    файл закривається й видаляється, а попередній звіт лишається без змін.
    Виняток не поглинається.
    """

    def __init__(self, path: Path) -> None:
        """Запам'ятовує цільовий шлях; файл відкривається у ``__enter__``."""
        self.path = path
        self._temp = path.with_name(path.name + ".tmp")
        self._file: TextIO | None = None

    @property
    def closed(self) -> bool:
        """Повертає ``True``, якщо файл не відкрито (ще не відкрито або вже закрито)."""
        return self._file is None or self._file.closed

    def __enter__(self) -> ReportWriter:
        """Створює каталог і відкриває тимчасовий файл у UTF-8 з ``\\n``."""
        self.path.parent.mkdir(parents=True, exist_ok=True)
        self._file = self._temp.open("w", encoding="utf-8", newline="\n")
        return self

    def write(self, text: str) -> None:
        """Записує текст у відкритий файл."""
        if self._file is None or self._file.closed:
            raise ValueError("Файл звіту не відкрито")
        self._file.write(text)

    def __exit__(
        self,
        exc_type: type[BaseException] | None,
        exc: BaseException | None,
        traceback: TracebackType | None,
    ) -> None:
        """Закриває файл; успіх - замінює звіт, виняток - прибирає чернетку."""
        if self._file is not None:
            self._file.close()
        if exc_type is None:
            self._temp.replace(self.path)
        else:
            with contextlib.suppress(FileNotFoundError):
                self._temp.unlink()


def write_report(path: Path, text: str) -> None:
    """Записує звіт через ``ReportWriter`` (UTF-8, ``\\n``, атомарна заміна)."""
    with ReportWriter(path) as writer:
        writer.write(text)
