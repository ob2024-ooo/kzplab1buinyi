"""Читання CSV і перетворення рядків на типізовані записи."""

from __future__ import annotations

import csv
import math
from collections.abc import Iterator
from pathlib import Path

from kzp_lab06.errors import DataError

REQUIRED_FIELDS = ("full_name", "group", "year", "average")

Record = dict[str, object]


def _clean_text(row: dict[str, str | None], field: str, row_number: int) -> str:
    """Повертає текстове поле без зайвих пробілів; порожнє значення — помилка."""
    value = row.get(field)
    # Пробіли по краях і подвійні пробіли всередині прибираються (нормалізація).
    text = " ".join((value or "").split())
    if not text:
        raise DataError(f"Рядок {row_number}: поле {field} порожнє")
    return text


def parse_row(row: dict[str, str | None], row_number: int) -> Record:
    """Перетворює один рядок CSV на запис і додає номер рядка до помилок.

    Args:
        row: словник від ``csv.DictReader`` (усі значення — рядки).
        row_number: номер рядка у файлі (заголовок має номер 1).

    Raises:
        DataError: якщо поле порожнє, не є числом або виходить за межі.
    """
    if None in row:
        raise DataError(f"Рядок {row_number}: забагато полів")
    full_name = _clean_text(row, "full_name", row_number)
    group = _clean_text(row, "group", row_number)
    raw_year = _clean_text(row, "year", row_number)
    raw_average = _clean_text(row, "average", row_number)
    try:
        # Перетворення виконується на межі входу, далі працюємо з числами.
        year = int(raw_year)
    except ValueError as error:
        raise DataError(
            f"Рядок {row_number}: поле year має бути цілим числом"
        ) from error
    if not 1 <= year <= 6:
        raise DataError(f"Рядок {row_number}: поле year має бути від 1 до 6")
    try:
        average = float(raw_average)
    except ValueError as error:
        raise DataError(f"Рядок {row_number}: поле average має бути числом") from error
    if math.isnan(average) or not 0 <= average <= 100:
        raise DataError(f"Рядок {row_number}: поле average має бути від 0 до 100")
    return {"full_name": full_name, "group": group, "year": year, "average": average}


def iter_records(path: Path) -> Iterator[Record]:
    """Читає CSV по одному запису (генератор), не завантажуючи весь файл.

    Правила: файл без заголовка — ``DataError``; файл лише із заголовком дає
    порожню послідовність; відсутній обов'язковий стовпець — ``DataError``.

    Raises:
        DataError: якщо формат або значення хибні.
    """
    # newline="" передає керування роздільниками рядків модулю csv.
    with path.open("r", encoding="utf-8", newline="") as source:
        reader = csv.DictReader(source)
        if reader.fieldnames is None:
            raise DataError("Порожній CSV: немає заголовка")
        missing = [name for name in REQUIRED_FIELDS if name not in reader.fieldnames]
        if missing:
            raise DataError("Відсутні стовпці: " + ", ".join(missing))
        try:
            for row in reader:
                # line_num — номер фізичного рядка, де закінчився запис.
                yield parse_row(row, reader.line_num)
        except csv.Error as error:
            raise DataError(f"Рядок {reader.line_num}: пошкоджений CSV") from error


def load_records(path: Path) -> list[Record]:
    """Читає весь CSV у список записів.

    Args:
        path: шлях до UTF-8 CSV із заголовком ``full_name,group,year,average``.
    """
    return list(iter_records(path))
