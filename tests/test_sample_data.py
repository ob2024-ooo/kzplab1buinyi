"""Перевірка реальних файлів data/ і еталонного звіту."""

from pathlib import Path

from kzp_lab06.cli import main

ROOT = Path(__file__).resolve().parents[1]


def test_sample_report_matches_baseline(tmp_path):
    output = tmp_path / "summary.txt"
    assert main([str(ROOT / "data" / "input.csv"), str(output)]) == 0
    expected = (ROOT / "baseline" / "summary.txt").read_text(encoding="utf-8")
    assert output.read_text(encoding="utf-8") == expected


def test_sample_error_files(tmp_path):
    out = str(tmp_path / "r.txt")
    assert main([str(ROOT / "data" / "broken.csv"), out]) == 1
    assert main([str(ROOT / "data" / "missing-column.csv"), out]) == 1
    assert main([str(ROOT / "data" / "empty.csv"), out]) == 0
