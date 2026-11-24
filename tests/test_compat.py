"""Сумісність із лабораторною №6: словникова й об'єктна моделі збігаються."""

from pathlib import Path

import pytest

from kzp_lab07.analysis import make_summary
from kzp_lab07.cli import main
from kzp_lab07.loader import load_learners
from kzp_lab07.reporting import format_report
from legacy_reference import legacy_summary, load_dicts

ROOT = Path(__file__).resolve().parents[1]
INPUT = ROOT / "data" / "input.csv"


def test_objects_match_dictionaries():
    old = legacy_summary(load_dicts(INPUT))
    new = make_summary(load_learners(INPUT))
    assert new.count == old["count"] == 8
    assert set(new.groups) == old["groups"]
    assert new.excellent_count == old["excellent_count"] == 4
    assert dict(new.excellent_by_group) == old["excellent_by_group"]
    assert new.average == pytest.approx(old["average"])
    assert (new.best_name, new.best_group, new.best_average) == (
        old["best"]["full_name"],
        old["best"]["group"],
        old["best"]["average"],
    )


@pytest.mark.parametrize("threshold", [0.0, 80.0, 90.0, 95.1, 101.0])
def test_threshold_equivalence(threshold):
    old = legacy_summary(load_dicts(INPUT), threshold)
    new = make_summary(load_learners(INPUT), threshold)
    assert new.excellent_count == old["excellent_count"]
    assert dict(new.excellent_by_group) == old["excellent_by_group"]


def test_report_equals_lab06_baseline(tmp_path):
    output = tmp_path / "summary.txt"
    assert main([str(INPUT), str(output)]) == 0
    expected = (ROOT / "baseline" / "summary.txt").read_text(encoding="utf-8")
    assert output.read_text(encoding="utf-8") == expected
    assert format_report(make_summary(load_learners(INPUT))) == expected


def test_ratings_report_matches_baseline(tmp_path):
    output = tmp_path / "ratings.txt"
    assert main([str(INPUT), str(output), "--ratings"]) == 0
    expected = (ROOT / "baseline" / "ratings.txt").read_text(encoding="utf-8")
    assert output.read_text(encoding="utf-8") == expected
    assert expected.startswith(
        (ROOT / "baseline" / "summary.txt").read_text(encoding="utf-8")
    )


def test_sample_error_files(tmp_path):
    out = str(tmp_path / "r.txt")
    assert main([str(ROOT / "data" / "broken.csv"), out]) == 1
    assert main([str(ROOT / "data" / "missing-column.csv"), out]) == 1
    assert main([str(ROOT / "data" / "empty.csv"), out]) == 0
