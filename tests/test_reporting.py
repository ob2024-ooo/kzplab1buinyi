"""Тести формування й запису звіту."""

from kzp_lab06.analysis import make_summary
from kzp_lab06.reporting import format_report, write_report

RECORDS = [
    {"full_name": "Іваненко Олена", "group": "КІ-303", "year": 3, "average": 92.5},
    {"full_name": "Петренко Андрій", "group": "КІ-301", "year": 2, "average": 78.0},
]


def test_format_report_text():
    assert format_report(make_summary(RECORDS)) == (
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


def test_report_does_not_depend_on_input_order():
    assert format_report(make_summary(RECORDS)) == format_report(
        make_summary(list(reversed(RECORDS)))
    )


def test_write_report_utf8_and_newlines(tmp_path):
    output = tmp_path / "nested" / "report.txt"
    write_report(output, "Готово\nКІ-301\n")
    assert output.read_bytes() == "Готово\nКІ-301\n".encode()


def test_write_report_overwrites(tmp_path):
    output = tmp_path / "report.txt"
    write_report(output, "перший\n")
    write_report(output, "другий\n")
    assert output.read_text(encoding="utf-8") == "другий\n"
