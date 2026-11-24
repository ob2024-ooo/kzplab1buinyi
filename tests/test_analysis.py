"""Тести аналізу: спільні функції працюють через протокол без перевірки типів."""

import inspect

import pytest

from kzp_lab07 import analysis
from kzp_lab07.analysis import (
    average_score,
    best_result,
    group_counts,
    group_ratings,
    make_summary,
    ranking,
    select_excellent,
    unique_groups,
)
from kzp_lab07.models import Bachelor, Master

LEARNERS = [
    Bachelor("А", "КІ-303", 3, 92.5),
    Bachelor("Б", "КІ-301", 2, 78.0),
    Master("В", "КІ-303", 5, 95.1),
    Master("Г", "КІ-301", 6, 90.0),
    Bachelor("А", "КІ-303", 3, 92.5),
]


class Duck:
    """Не є підтипом Learner, але має потрібні властивості та метод rating()."""

    def __init__(self, name, group, average, rating):
        self.full_name = name
        self.group = group
        self.average = average
        self._rating = rating

    def rating(self):
        return self._rating


def test_analysis_has_no_subtype_checks():
    source = inspect.getsource(analysis)
    for forbidden in ("isinstance", "Bachelor", "Master", "type("):
        assert forbidden not in source


def test_select_excellent_includes_boundary():
    assert [x.full_name for x in select_excellent(LEARNERS)] == ["А", "В", "Г", "А"]
    assert len(select_excellent(LEARNERS, 95.0)) == 1


def test_unique_groups_and_counts_with_duplicates():
    assert unique_groups(LEARNERS) == {"КІ-301", "КІ-303"}
    counts = group_counts(LEARNERS)
    assert counts == {"КІ-301": 2, "КІ-303": 3}
    assert list(counts) == sorted(counts)


def test_empty_input():
    assert group_counts([]) == {}
    assert unique_groups([]) == set()
    assert average_score([]) == 0.0
    assert best_result([]) is None
    assert ranking([]) == []
    assert group_ratings([]) == ()
    summary = make_summary([])
    assert summary.count == 0
    assert summary.best_name is None


def test_single_object():
    summary = make_summary([Bachelor("А", "Г", 1, 100.0)])
    assert summary.count == 1
    assert summary.best_name == "А"
    assert summary.excellent_count == 1


def test_best_result_tie_is_deterministic():
    tie = [Bachelor("Борис", "Г", 1, 90.0), Bachelor("Андрій", "Г", 1, 90.0)]
    assert best_result(tie).full_name == "Андрій"
    assert best_result(list(reversed(tie))).full_name == "Андрій"


def test_ranking_uses_polymorphic_rating():
    ordered = ranking(LEARNERS)
    # Магістри мають вищу вагу балу й надбавку, тому йдуть першими.
    assert [x.full_name for x in ordered][:2] == ["В", "Г"]
    ratings = [x.rating() for x in ordered]
    assert ratings == sorted(ratings, reverse=True)


def test_group_ratings_mix_subtypes():
    result = {item.group: item for item in group_ratings(LEARNERS)}
    expected_303 = (
        LEARNERS[0].rating() + LEARNERS[2].rating() + LEARNERS[4].rating()
    ) / 3
    assert result["КІ-303"].count == 3
    assert result["КІ-303"].average_rating == pytest.approx(expected_303)
    assert list(result) == ["КІ-301", "КІ-303"]


def test_duck_typing_objects_work_in_all_functions():
    ducks = [Duck("Х", "Г1", 95.0, 10.0), Duck("Я", "Г2", 80.0, 20.0)]
    summary = make_summary(ducks)
    assert summary.excellent_count == 1
    assert summary.best_name == "Х"
    assert [d.full_name for d in ranking(ducks)] == ["Я", "Х"]
    assert group_ratings(ducks)[0].average_rating == 10.0


def test_make_summary_values_and_input_not_modified():
    before = [repr(x) for x in LEARNERS]
    summary = make_summary(LEARNERS)
    assert summary.count == 5
    assert summary.groups == {"КІ-301", "КІ-303"}
    assert summary.excellent_by_group == (("КІ-301", 1), ("КІ-303", 3))
    assert (summary.best_name, summary.best_average) == ("В", 95.1)
    assert [repr(x) for x in LEARNERS] == before
