"""Тести функцій аналізу."""

import pytest

from kzp_lab06.analysis import (
    average_score,
    best_result,
    group_counts,
    make_summary,
    select_excellent,
    stream_summary,
    summarize,
    unique_groups,
)


def rec(name, group, year, average):
    return {"full_name": name, "group": group, "year": year, "average": average}


RECORDS = [
    rec("А", "КІ-303", 3, 92.5),
    rec("Б", "КІ-301", 2, 78.0),
    rec("В", "КІ-303", 5, 95.1),
    rec("Г", "КІ-301", 6, 90.0),
    rec("А", "КІ-303", 3, 92.5),
]


def test_select_excellent_includes_boundary():
    assert [r["full_name"] for r in select_excellent(RECORDS)] == ["А", "В", "Г", "А"]


def test_select_with_custom_threshold():
    assert len(select_excellent(RECORDS, 95.0)) == 1


def test_unique_groups_is_a_set():
    assert unique_groups(RECORDS) == {"КІ-301", "КІ-303"}


def test_group_counts_are_sorted_and_count_duplicates():
    counts = group_counts(RECORDS)
    assert counts == {"КІ-301": 2, "КІ-303": 3}
    assert list(counts) == sorted(counts)


def test_empty_input():
    assert group_counts([]) == {}
    assert unique_groups([]) == set()
    assert average_score([]) == 0.0
    assert best_result([]) is None


def test_single_record():
    summary = make_summary([rec("А", "Г", 1, 100.0)])
    assert summary["count"] == 1
    assert summary["best"]["full_name"] == "А"


def test_average_score():
    assert average_score(RECORDS) == pytest.approx((92.5 + 78 + 95.1 + 90 + 92.5) / 5)


def test_best_result_tie_is_deterministic():
    tie = [rec("Борис", "Г", 1, 90.0), rec("Андрій", "Г", 1, 90.0)]
    assert best_result(tie)["full_name"] == "Андрій"
    assert best_result(list(reversed(tie)))["full_name"] == "Андрій"


def test_summarize_defaults_args_and_kwargs():
    result = summarize(
        RECORDS, "average", "year", minimum_average=90.0, average="Сума", year="Курси"
    )
    assert result["count"] == 4
    assert result["Сума"] == pytest.approx(92.5 + 95.1 + 90 + 92.5)
    assert result["Курси"] == 3 + 5 + 6 + 3


def test_summarize_without_metrics_has_only_count():
    assert summarize(RECORDS) == {"count": 5}


def test_summarize_empty_selection_gives_zero():
    result = summarize(RECORDS, "average", minimum_average=101.0)
    assert result == {"count": 0, "average": 0.0}


def test_summarize_unknown_metric():
    with pytest.raises(ValueError, match="Невідома метрика: price"):
        summarize(RECORDS, "price")
    with pytest.raises(ValueError, match="Невідома метрика"):
        summarize([], "price")


def test_make_summary_values():
    summary = make_summary(RECORDS)
    assert summary["count"] == 5
    assert summary["groups"] == {"КІ-301", "КІ-303"}
    assert summary["excellent_count"] == 4
    assert summary["excellent_by_group"] == {"КІ-301": 1, "КІ-303": 3}
    assert summary["best"]["full_name"] == "В"


def test_stream_summary_equals_list_summary():
    for records in (RECORDS, []):
        streamed = stream_summary(iter(records))
        plain = make_summary(records)
        # Суми з плаваючою комою можуть відрізнятися в останньому біті.
        assert streamed.pop("average") == pytest.approx(plain.pop("average"))
        assert streamed == plain


def test_input_is_not_modified():
    before = [dict(r) for r in RECORDS]
    make_summary(RECORDS)
    assert before == RECORDS
