"""Модель предметної області: студенти, підтипи, протокол і підсумки.

Модуль не читає файли й не будує звіт.
"""

from __future__ import annotations

import math
from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Protocol


class Rated(Protocol):
    """Протокол об'єкта з назвою, групою, балом і рейтингом.

    Функції аналізу залежать лише від цього протоколу (качина типізація),
    а не від конкретних класів.
    """

    @property
    def full_name(self) -> str:
        """Повертає ПІБ."""
        ...

    @property
    def group(self) -> str:
        """Повертає групу."""
        ...

    @property
    def average(self) -> float:
        """Повертає середній бал."""
        ...

    def rating(self) -> float:
        """Повертає рейтинг об'єкта."""
        ...


def _clean(value: str, field: str) -> str:
    """Нормалізує текст (зайві пробіли) і відхиляє порожнє значення."""
    text = " ".join(str(value).split())
    if not text:
        raise ValueError(f"поле {field} порожнє")
    return text


class Learner(ABC):
    """Базова сутність: студент зі спільним станом і перевірками.

    Спільна операція ``rating()`` реалізується в підтипах. Діапазон курсів
    задають атрибути класу ``MIN_YEAR`` і ``MAX_YEAR``.
    """

    MIN_YEAR = 1
    MAX_YEAR = 6

    def __init__(self, full_name: str, group: str, year: int, average: float) -> None:
        """Створює студента; значення проходять через setter-и.

        Raises:
            ValueError: якщо будь-яке значення неприпустиме.
        """
        self.full_name = full_name
        self.group = group
        self.year = year
        self.average = average

    @property
    def full_name(self) -> str:
        """Повертає ПІБ."""
        return self._full_name

    @full_name.setter
    def full_name(self, value: str) -> None:
        """Встановлює непорожнє ПІБ без зайвих пробілів."""
        # Запис іде у внутрішнє поле, інакше setter викликав би сам себе.
        self._full_name = _clean(value, "full_name")

    @property
    def group(self) -> str:
        """Повертає групу."""
        return self._group

    @group.setter
    def group(self, value: str) -> None:
        """Встановлює непорожню групу."""
        self._group = _clean(value, "group")

    @property
    def year(self) -> int:
        """Повертає курс."""
        return self._year

    @year.setter
    def year(self, value: int) -> None:
        """Встановлює курс у межах, дозволених для підтипу."""
        if isinstance(value, bool) or not isinstance(value, int):
            raise ValueError("поле year має бути цілим числом")
        if not self.MIN_YEAR <= value <= self.MAX_YEAR:
            raise ValueError(
                f"поле year має бути від {self.MIN_YEAR} до {self.MAX_YEAR}"
            )
        self._year = value

    @property
    def average(self) -> float:
        """Повертає середній бал."""
        return self._average

    @average.setter
    def average(self, value: float) -> None:
        """Встановлює середній бал від 0 до 100 (межі входять)."""
        number = float(value)
        if math.isnan(number) or not 0 <= number <= 100:
            raise ValueError("поле average має бути від 0 до 100")
        self._average = number

    @abstractmethod
    def rating(self) -> float:
        """Обчислює рейтинг; формула залежить від підтипу."""

    def _state(self) -> tuple[str, str, int, float]:
        """Повертає повний стан для порівняння."""
        return (self.full_name, self.group, self.year, self.average)

    def __str__(self) -> str:
        """Повертає коротке подання для читача."""
        return f"{self.full_name} ({self.group}, курс {self.year}): {self.average:.2f}"

    def __repr__(self) -> str:
        """Повертає діагностичне подання об'єкта."""
        return (
            f"{type(self).__name__}(full_name={self.full_name!r}, "
            f"group={self.group!r}, year={self.year!r}, average={self.average!r})"
        )

    def __eq__(self, other: object) -> bool:
        """Порівнює студентів одного підтипу за повним станом."""
        if not isinstance(other, Learner):
            return NotImplemented
        return type(self) is type(other) and self._state() == other._state()

    # Стан змінюваний (setter-и), тому об'єкт не можна використовувати як ключ.
    __hash__ = None  # type: ignore[assignment]


class Bachelor(Learner):
    """Студент-бакалавр: курси 1-4, вага балу 1.0, надбавка 0.5 за курс."""

    MIN_YEAR = 1
    MAX_YEAR = 4
    AVERAGE_WEIGHT = 1.0
    YEAR_BONUS = 0.5

    def rating(self) -> float:
        """Обчислює рейтинг бакалавра."""
        return self.average * self.AVERAGE_WEIGHT + self.year * self.YEAR_BONUS


class Master(Learner):
    """Студент-магістр: курси 5-6, вага балу 1.1, надбавка 2.0 за курс магістратури."""

    MIN_YEAR = 5
    MAX_YEAR = 6
    AVERAGE_WEIGHT = 1.1
    YEAR_BONUS = 2.0

    def rating(self) -> float:
        """Обчислює рейтинг магістра."""
        return self.average * self.AVERAGE_WEIGHT + (self.year - 4) * self.YEAR_BONUS


@dataclass(frozen=True)
class GroupRating:
    """Незмінний рейтинг групи: кількість студентів і середній рейтинг."""

    group: str
    count: int
    average_rating: float


@dataclass(frozen=True)
class LearnerSummary:
    """Незмінний підсумок реєстру (замінює словник підсумку з лабораторної №6)."""

    count: int
    groups: frozenset[str]
    threshold: float
    excellent_count: int
    excellent_by_group: tuple[tuple[str, int], ...]
    average: float
    best_name: str | None
    best_group: str | None
    best_average: float | None
    group_ratings: tuple[GroupRating, ...]
