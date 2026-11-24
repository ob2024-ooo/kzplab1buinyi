"""Тести моделі: властивості, setter-и, dataclass, спеціальні методи, підтипи."""

import dataclasses
import math

import pytest

from kzp_lab07.models import Bachelor, GroupRating, Learner, LearnerSummary, Master


def bachelor(**changes):
    values = {
        "full_name": "Іваненко Олена",
        "group": "КІ-303",
        "year": 3,
        "average": 92.5,
    }
    values.update(changes)
    return Bachelor(**values)


def test_constructor_sets_properties():
    learner = bachelor()
    assert (learner.full_name, learner.group, learner.year, learner.average) == (
        "Іваненко Олена",
        "КІ-303",
        3,
        92.5,
    )


def test_text_is_normalized():
    learner = bachelor(full_name="  Іваненко   Олена ", group=" КІ-303 ")
    assert learner.full_name == "Іваненко Олена"
    assert learner.group == "КІ-303"


@pytest.mark.parametrize("value", [0, 0.0, 50, 100, 100.0])
def test_average_setter_accepts_valid_and_boundary_values(value):
    learner = bachelor()
    learner.average = value
    assert learner.average == float(value)


@pytest.mark.parametrize("value", [-0.1, 100.1, math.nan, -1])
def test_average_setter_rejects_invalid_values(value):
    learner = bachelor()
    with pytest.raises(ValueError, match="від 0 до 100"):
        learner.average = value
    assert learner.average == 92.5  # старе значення збережено


@pytest.mark.parametrize("value", ["", "   "])
def test_text_setters_reject_blank(value):
    learner = bachelor()
    with pytest.raises(ValueError, match="full_name порожнє"):
        learner.full_name = value
    with pytest.raises(ValueError, match="group порожнє"):
        learner.group = value


@pytest.mark.parametrize(("cls", "low", "high"), [(Bachelor, 1, 4), (Master, 5, 6)])
def test_year_boundaries_per_subtype(cls, low, high):
    learner = cls("О", "Г", low, 50)
    learner.year = high
    assert learner.year == high
    for bad in (low - 1, high + 1):
        with pytest.raises(ValueError, match=f"від {low} до {high}"):
            learner.year = bad


@pytest.mark.parametrize("bad", ["3", 3.5, True, None])
def test_year_must_be_int(bad):
    with pytest.raises(ValueError, match="цілим числом"):
        bachelor(year=bad)


def test_constructor_rejects_invalid_state():
    with pytest.raises(ValueError):
        bachelor(average=101)
    with pytest.raises(ValueError):
        Master("О", "Г", 3, 80)  # курс 3 не належить магістру


def test_base_class_is_abstract():
    with pytest.raises(TypeError):
        Learner("О", "Г", 1, 50)  # type: ignore[abstract]


def test_subtypes_have_different_ratings_for_same_data():
    b = Bachelor("О", "Г", 4, 80)
    m = Master("О", "Г", 5, 80)
    assert b.rating() == pytest.approx(80 * 1.0 + 4 * 0.5)
    assert m.rating() == pytest.approx(80 * 1.1 + 1 * 2.0)
    assert b.rating() != m.rating()


def test_rating_follows_setter_changes():
    learner = bachelor()
    before = learner.rating()
    learner.average = 50
    assert learner.rating() < before


def test_str_and_repr():
    learner = bachelor()
    assert str(learner) == "Іваненко Олена (КІ-303, курс 3): 92.50"
    assert repr(learner) == (
        "Bachelor(full_name='Іваненко Олена', group='КІ-303', year=3, average=92.5)"
    )


def test_equality_for_duplicates():
    assert bachelor() == bachelor()
    assert bachelor() != bachelor(average=92.4)


def test_equality_distinguishes_subtypes():
    assert Bachelor("О", "Г", 4, 80) != Master("О", "Г", 5, 80)


def test_equality_with_foreign_type_returns_notimplemented():
    assert bachelor().__eq__(5) is NotImplemented
    assert bachelor() != 5


def test_learner_is_unhashable_because_state_is_mutable():
    with pytest.raises(TypeError):
        hash(bachelor())


def test_summary_is_frozen_and_comparable():
    summary = LearnerSummary(
        2, frozenset({"А"}), 90.0, 1, (("А", 1),), 80.0, "О", "А", 90.0, ()
    )
    with pytest.raises(dataclasses.FrozenInstanceError):
        summary.count = 3  # type: ignore[misc]
    assert summary == dataclasses.replace(summary)
    assert summary != dataclasses.replace(summary, count=3)


def test_group_rating_is_frozen_hashable_value():
    rating = GroupRating("КІ-301", 2, 80.5)
    assert rating == GroupRating("КІ-301", 2, 80.5)
    assert {rating: 1}[GroupRating("КІ-301", 2, 80.5)] == 1
    with pytest.raises(dataclasses.FrozenInstanceError):
        rating.count = 5  # type: ignore[misc]
    assert "GroupRating(group='КІ-301'" in repr(rating)
