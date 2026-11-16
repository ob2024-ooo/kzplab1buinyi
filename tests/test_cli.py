"""Тести CLI: коди завершення, повідомлення й запуск як модуля."""

import subprocess
import sys
from pathlib import Path

from kzp_lab06.cli import main

HEADER = "full_name,group,year,average\n"
ROOT = Path(__file__).resolve().parents[1]


def test_success_exit_code_and_file(write_csv, tmp_path, capsys):
    source = write_csv(HEADER + "Олена,КІ-303,3,92.5\n")
    output = tmp_path / "out" / "report.txt"
    assert main([str(source), str(output)]) == 0
    assert "Записів: 1" in output.read_text(encoding="utf-8")
    assert "Звіт записано" in capsys.readouterr().out


def test_empty_csv_gives_valid_report(write_csv, tmp_path):
    output = tmp_path / "report.txt"
    assert main([str(write_csv(HEADER)), str(output)]) == 0
    assert "Записів: 0" in output.read_text(encoding="utf-8")


def test_broken_csv_returns_1_and_message(write_csv, tmp_path, capsys):
    source = write_csv(HEADER + "Олена,КІ-303,3,abc\n")
    output = tmp_path / "report.txt"
    assert main([str(source), str(output)]) == 1
    err = capsys.readouterr().err
    assert "Рядок 2: поле average має бути числом" in err
    assert "Причина:" in err
    assert not output.exists()


def test_missing_input_file_returns_3(tmp_path, capsys):
    assert main([str(tmp_path / "немає.csv"), str(tmp_path / "r.txt")]) == 3
    assert "Помилка файла" in capsys.readouterr().err


def test_input_is_not_modified(write_csv, tmp_path):
    source = write_csv(HEADER + "Олена,КІ-303,3,92.5\n")
    before = source.read_bytes()
    main([str(source), str(tmp_path / "r.txt")])
    assert source.read_bytes() == before


def test_stream_option_gives_same_report(write_csv, tmp_path):
    source = write_csv(
        HEADER + "Олена,КІ-303,3,92.5\nПетро,КІ-301,2,75\nІрина,КІ-303,5,95.1\n"
    )
    plain = tmp_path / "a.txt"
    streamed = tmp_path / "b.txt"
    assert main([str(source), str(plain)]) == 0
    assert main([str(source), str(streamed), "--stream"]) == 0
    assert plain.read_bytes() == streamed.read_bytes()


def test_threshold_option(write_csv, tmp_path):
    source = write_csv(HEADER + "Олена,КІ-303,3,92.5\n")
    output = tmp_path / "r.txt"
    assert main([str(source), str(output), "--threshold", "95"]) == 0
    assert "Відмінників (середній бал >= 95.0): 0" in output.read_text(encoding="utf-8")


def test_usage_error_is_nonzero():
    try:
        main([])
    except SystemExit as exit_info:
        assert exit_info.code == 2
    else:
        raise AssertionError("очікувався SystemExit")


def test_run_as_module(tmp_path):
    output = tmp_path / "report.txt"
    result = subprocess.run(
        [sys.executable, "-m", "kzp_lab06.cli", "data/input.csv", str(output)],
        cwd=ROOT,
        capture_output=True,
        env={"PYTHONPATH": str(ROOT / "src"), "PATH": "", "PYTHONIOENCODING": "utf-8"},
        check=False,
    )
    assert result.returncode == 0
    assert "Найкращий результат: Лисенко Ірина (КІ-303), 95.10" in output.read_text(
        encoding="utf-8"
    )


def test_import_has_no_side_effects(capsys):
    import importlib

    import kzp_lab06.cli

    importlib.reload(kzp_lab06.cli)
    assert capsys.readouterr().out == ""
