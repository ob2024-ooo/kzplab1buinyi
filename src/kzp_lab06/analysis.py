"""Обчислення над записами. Модуль не відкриває файли й нічого не друкує."""

from __future__ import annotations

from collections.abc import Iterable
from typing import Any

from kzp_lab06.loader import Record

THRESHOLD = 90.0
NUMERIC_FIELDS = ("year", "average")


def select_excellent(records: list[Record], minimum: float = THRESHOLD) -> list[Record]:
    """Відбирає записи із середнім балом не нижче ``minimum``."""
    return [record for record in records if float(record["average"]) >= minimum]


def unique_groups(records: list[Record]) -> set[str]:
    """Повертає множину унікальних груп."""
    return {str(record["group"]) for record in records}


def group_counts(records: list[Record]) -> dict[str, int]:
    """Рахує записи за групами; ключі впорядковано для стабільного звіту."""
    groups = unique_groups(records)
    return {
        group: sum(1 for record in records if record["group"] == group)
        for group in sorted(groups)
    }


def average_score(records: list[Record]) -> float:
    """Середній бал; для порожнього списку — 0.0."""
    if not records:
        return 0.0
    return sum(float(record["average"]) for record in records) / len(records)


def best_result(records: list[Record]) -> Record | None:
    """Запис із найвищим балом; за рівності — за ПІБ і групою (детермінованість)."""
    if not records:
        return None
    return min(
        records,
        key=lambda r: (-float(r["average"]), str(r["full_name"]), str(r["group"])),
    )


def summarize(
    records: list[Record],
    *metrics: str,
    minimum_average: float = 0.0,
    **labels: str,
) -> dict[str, object]:
    """Обчислює суми вибраних числових полів після фільтрування за балом.

    Args:
        records: записи після завантаження.
        *metrics: назви числових полів (``year``, ``average``).
        minimum_average: нижня межа середнього балу для відбору.
        **labels: підписи для метрик (ключ — назва метрики).

    Raises:
        ValueError: якщо назва метрики невідома.
    """
    for metric in metrics:
        if metric not in NUMERIC_FIELDS:
            raise ValueError(f"Невідома метрика: {metric}")
    selected = [r for r in records if float(r["average"]) >= minimum_average]
    result: dict[str, object] = {"count": len(selected)}
    for metric in metrics:
        # Порожній відбір дає 0.0, а не помилку.
        result[labels.get(metric, metric)] = sum(float(r[metric]) for r in selected)
    return result


def make_summary(
    records: list[Record], threshold: float = THRESHOLD
) -> dict[str, object]:
    """Збирає всі показники варіанта 6 у словник."""
    excellent = select_excellent(records, threshold)
    return {
        "count": len(records),
        "groups": unique_groups(records),
        "threshold": threshold,
        "excellent_count": len(excellent),
        "excellent_by_group": group_counts(excellent),
        "average": average_score(records),
        "best": best_result(records),
    }


def stream_summary(
    records: Iterable[Record], threshold: float = THRESHOLD
) -> dict[str, Any]:
    """Той самий підсумок за один прохід, без збереження записів у пам'яті."""
    count = 0
    total = 0.0
    groups: set[str] = set()
    excellent_by_group: dict[str, int] = {}
    best: Record | None = None
    for record in records:
        count += 1
        average = float(record["average"])
        total += average
        groups.add(str(record["group"]))
        if average >= threshold:
            group = str(record["group"])
            excellent_by_group[group] = excellent_by_group.get(group, 0) + 1
        if best is None or _best_key(record) < _best_key(best):
            best = record
    return {
        "count": count,
        "groups": groups,
        "threshold": threshold,
        "excellent_count": sum(excellent_by_group.values()),
        "excellent_by_group": dict(sorted(excellent_by_group.items())),
        "average": total / count if count else 0.0,
        "best": best,
    }


def _best_key(record: Record) -> tuple[float, str, str]:
    """Ключ порівняння для пошуку найкращого результату."""
    return (-float(record["average"]), str(record["full_name"]), str(record["group"]))
