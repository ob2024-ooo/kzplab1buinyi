"""Тести звіту та контекстного менеджера ReportWriter."""

import pytest

from kzp_lab07.analysis import make_summary
from kzp_lab07.models import Bachelor, Master
from kzp_lab07.reporting import ReportWriter, format_report, write_report

LEARNERS = [
    Bachelor("Іваненко Олена", "КІ-303", 3, 92.5),
    Bachelor("Петренко Андрій", "КІ-301", 2, 78.0),
]


def test_format_report_text():
    assert format_report(make_summary(LEARNERS)) == (
        "Реєстр студентів: звіт\n"
        "Записів: 2\n"
        "Груп: 2\n"
        "Відмінників (середній бал >= 90.0): 1\n"
        "Відмінники за групами:\n"
        "КІ-303: 1\n"
        "Середній бал: 85.25\n"
        "Найкращий результат: Іваненко Олена (КІ-303), 92.50\n"
    )


def test_format_empty_report():
    assert format_report(make_summary([])) == (
        "Реєстр студентів: звіт\n"
        "Записів: 0\n"
        "Груп: 0\n"
        "Відмінників (середній бал >= 90.0): 0\n"
        "Середній бал: 0.00\n"
        "Найкращий результат: немає даних\n"
    )


def test_ratings_section():
    text = format_report(make_summary(LEARNERS + [Master("М", "КІ-303", 5, 80)]), True)
    assert text.endswith("Рейтинг груп:\nКІ-301: 79.00 (1)\nКІ-303: 92.00 (2)\n")


def test_report_does_not_depend_on_input_order():
    assert format_report(make_summary(LEARNERS)) == format_report(
        make_summary(list(reversed(LEARNERS)))
    )


def test_write_report_utf8_and_newlines(tmp_path):
    output = tmp_path / "nested" / "report.txt"
    write_report(output, "Готово\nКІ-301\n")
    assert output.read_bytes() == "Готово\nКІ-301\n".encode()
    assert not (tmp_path / "nested" / "report.txt.tmp").exists()


def test_write_report_overwrites(tmp_path):
    output = tmp_path / "report.txt"
    write_report(output, "перший\n")
    write_report(output, "другий\n")
    assert output.read_text(encoding="utf-8") == "другий\n"


def test_writer_closes_file_after_success(tmp_path):
    writer = ReportWriter(tmp_path / "r.txt")
    assert writer.closed
    with writer:
        assert not writer.closed
        writer.write("текст\n")
    assert writer.closed
    assert (tmp_path / "r.txt").read_text(encoding="utf-8") == "текст\n"


def test_writer_closes_file_and_propagates_exception(tmp_path):
    output = tmp_path / "r.txt"
    writer = ReportWriter(output)
    with pytest.raises(RuntimeError, match="збій"), writer:
        writer.write("частковий звіт")
        raise RuntimeError("збій")
    assert writer.closed
    assert not output.exists()
    assert not (tmp_path / "r.txt.tmp").exists()


def test_writer_keeps_previous_report_on_error(tmp_path):
    output = tmp_path / "r.txt"
    output.write_text("старий\n", encoding="utf-8")
    with pytest.raises(ValueError), ReportWriter(output) as writer:
        writer.write("новий, але незавершений")
        raise ValueError
    assert output.read_text(encoding="utf-8") == "старий\n"


def test_writer_rejects_write_when_closed(tmp_path):
    writer = ReportWriter(tmp_path / "r.txt")
    with pytest.raises(ValueError, match="не відкрито"):
        writer.write("x")
