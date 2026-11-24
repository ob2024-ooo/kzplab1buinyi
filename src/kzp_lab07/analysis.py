"""Обчислення над об'єктами. Працює лише через протокол ``Rated``.

У модулі немає перевірок конкретних підтипів: формулу рейтингу знає підтип.
Модуль не відкриває файли й не друкує.
"""

from __future__ import annotations

from collections.abc import Sequence

from kzp_lab07.models import GroupRating, LearnerSummary, Rated

THRESHOLD = 90.0


def select_excellent(
    learners: Sequence[Rated], minimum: float = THRESHOLD
) -> list[Rated]:
    """Відбирає студентів із середнім балом не нижче ``minimum``."""
    return [learner for learner in learners if learner.average >= minimum]


def unique_groups(learners: Sequence[Rated]) -> set[str]:
    """Повертає множину унікальних груп."""
    return {learner.group for learner in learners}


def group_counts(learners: Sequence[Rated]) -> dict[str, int]:
    """Рахує студентів за групами; ключі впорядковано для стабільного звіту."""
    groups = unique_groups(learners)
    return {
        group: sum(1 for learner in learners if learner.group == group)
        for group in sorted(groups)
    }


def average_score(learners: Sequence[Rated]) -> float:
    """Середній бал; для порожнього списку 0.0."""
    if not learners:
        return 0.0
    return sum(learner.average for learner in learners) / len(learners)


def ranking_key(learner: Rated) -> tuple[float, str, str]:
    """Ключ впорядкування: спершу вищий рейтинг, далі ПІБ і група."""
    return (-learner.rating(), learner.full_name, learner.group)


def ranking(learners: Sequence[Rated]) -> list[Rated]:
    """Повертає студентів у порядку спадання рейтингу."""
    return sorted(learners, key=ranking_key)


def best_result(learners: Sequence[Rated]) -> Rated | None:
    """Студент із найвищим середнім балом; за рівності - за ПІБ і групою."""
    return min(
        learners,
        key=lambda learner: (-learner.average, learner.full_name, learner.group),
        default=None,
    )


def group_ratings(learners: Sequence[Rated]) -> tuple[GroupRating, ...]:
    """Середній рейтинг за групами (рейтинг групи), групи відсортовано."""
    result = []
    for group in sorted(unique_groups(learners)):
        members = [learner for learner in learners if learner.group == group]
        total = sum(member.rating() for member in members)
        result.append(GroupRating(group, len(members), total / len(members)))
    return tuple(result)


def make_summary(
    learners: Sequence[Rated], threshold: float = THRESHOLD
) -> LearnerSummary:
    """Збирає незмінний підсумок варіанта 6."""
    excellent = select_excellent(learners, threshold)
    best = best_result(learners)
    return LearnerSummary(
        count=len(learners),
        groups=frozenset(unique_groups(learners)),
        threshold=threshold,
        excellent_count=len(excellent),
        excellent_by_group=tuple(group_counts(excellent).items()),
        average=average_score(learners),
        best_name=best.full_name if best else None,
        best_group=best.group if best else None,
        best_average=best.average if best else None,
        group_ratings=group_ratings(learners),
    )
