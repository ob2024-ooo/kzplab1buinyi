"""Читання CSV і перетворення рядків на об'єкти ``Bachelor`` та ``Master``."""

from __future__ import annotations

import csv
from pathlib import Path

from kzp_lab07.errors import DataError
from kzp_lab07.models import Bachelor, Learner, Master

REQUIRED_FIELDS = ("full_name", "group", "year", "average")
BACHELOR_MAX_YEAR = Bachelor.MAX_YEAR


def _text(row: dict[str, str | None], field: str, row_number: int) -> str:
    """Повертає значення поля; відсутнє або порожнє - помилка з номером рядка."""
    value = row.get(field)
    if value is None or not value.strip():
        raise DataError(f"Рядок {row_number}: поле {field} порожнє")
    return value


def build_learner(row: dict[str, str | None], row_number: int) -> Learner:
    """Перетворює рядок CSV на об'єкт відповідного підтипу.

    Підтип обирається тут, на межі завантаження: курси 1-4 дають ``Bachelor``,
    5-6 - ``Master``. Правила значень перевіряють setter-и моделі.

    Raises:
        DataError: з номером рядка, назвою поля та першопричиною.
    """
    if None in row:
        raise DataError(f"Рядок {row_number}: забагато полів")
    full_name = _text(row, "full_name", row_number)
    group = _text(row, "group", row_number)
    raw_year = _text(row, "year", row_number)
    raw_average = _text(row, "average", row_number)
    try:
        year = int(raw_year)
    except ValueError as error:
        raise DataError(
            f"Рядок {row_number}: поле year має бути цілим числом"
        ) from error
    try:
        average = float(raw_average)
    except ValueError as error:
        raise DataError(f"Рядок {row_number}: поле average має бути числом") from error
    if not Learner.MIN_YEAR <= year <= Learner.MAX_YEAR:
        # Той самий текст помилки, що й у лабораторній №6.
        raise DataError(
            f"Рядок {row_number}: поле year має бути від "
            f"{Learner.MIN_YEAR} до {Learner.MAX_YEAR}"
        )
    kind = Bachelor if year <= BACHELOR_MAX_YEAR else Master
    try:
        return kind(full_name, group, year, average)
    except ValueError as error:
        raise DataError(f"Рядок {row_number}: {error}") from error


def load_learners(path: Path) -> list[Learner]:
    """Читає CSV (UTF-8, заголовок ``full_name,group,year,average``).

    Правила: файл без заголовка - ``DataError``; лише заголовок дає порожній
    список; відсутній обов'язковий стовпець - ``DataError``.
    """
    # newline="" передає керування роздільниками рядків модулю csv.
    with path.open("r", encoding="utf-8", newline="") as source:
        reader = csv.DictReader(source)
        if reader.fieldnames is None:
            raise DataError("Порожній CSV: немає заголовка")
        missing = [name for name in REQUIRED_FIELDS if name not in reader.fieldnames]
        if missing:
            raise DataError("Відсутні стовпці: " + ", ".join(missing))
        learners: list[Learner] = []
        try:
            for row in reader:
                # line_num - номер фізичного рядка, де закінчився запис.
                learners.append(build_learner(row, reader.line_num))
        except csv.Error as error:
            raise DataError(f"Рядок {reader.line_num}: пошкоджений CSV") from error
        return learners
