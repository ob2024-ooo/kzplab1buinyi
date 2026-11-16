"""Тести завантаження й перетворення CSV."""

import pytest

from kzp_lab06.errors import DataError
from kzp_lab06.loader import iter_records, load_records, parse_row

HEADER = "full_name,group,year,average\n"


def test_load_valid_rows(write_csv):
    path = write_csv(HEADER + "Іваненко Олена,КІ-303,3,92.5\n")
    assert load_records(path) == [
        {"full_name": "Іваненко Олена", "group": "КІ-303", "year": 3, "average": 92.5}
    ]


def test_header_only_gives_empty_list(write_csv):
    assert load_records(write_csv(HEADER)) == []


def test_completely_empty_file_is_error(write_csv):
    with pytest.raises(DataError, match="немає заголовка"):
        load_records(write_csv(""))


def test_missing_column(write_csv):
    path = write_csv("full_name,group,year\nОлена,КІ-303,3\n")
    with pytest.raises(DataError, match="average"):
        load_records(path)


def test_missing_value_in_row(write_csv):
    path = write_csv(HEADER + "Олена,КІ-303,3\n")
    with pytest.raises(DataError, match="Рядок 2: поле average порожнє"):
        load_records(path)


def test_extra_fields_in_row(write_csv):
    path = write_csv(HEADER + "Олена,КІ-303,3,90,зайве\n")
    with pytest.raises(DataError, match="забагато полів"):
        load_records(path)


def test_bad_number_keeps_cause_and_row_number(write_csv):
    path = write_csv(HEADER + "Олена,КІ-303,3,90\nПетро,КІ-301,2,abc\n")
    with pytest.raises(DataError, match="Рядок 3: поле average") as info:
        load_records(path)
    assert isinstance(info.value.__cause__, ValueError)


def test_bad_year(write_csv):
    path = write_csv(HEADER + "Олена,КІ-303,x,90\n")
    with pytest.raises(DataError, match="year має бути цілим") as info:
        load_records(path)
    assert isinstance(info.value.__cause__, ValueError)


@pytest.mark.parametrize("year", ["0", "7", "-1"])
def test_year_out_of_range(year):
    with pytest.raises(DataError, match="від 1 до 6"):
        parse_row({"full_name": "О", "group": "Г", "year": year, "average": "50"}, 2)


@pytest.mark.parametrize("average", ["-0.1", "100.1", "nan"])
def test_average_out_of_range(average):
    with pytest.raises(DataError, match="від 0 до 100"):
        parse_row({"full_name": "О", "group": "Г", "year": "1", "average": average}, 2)


@pytest.mark.parametrize("average", ["0", "100"])
def test_average_boundaries_are_valid(average):
    record = parse_row(
        {"full_name": "О", "group": "Г", "year": "1", "average": average}, 2
    )
    assert record["average"] == float(average)


def test_comma_quotes_and_newline_in_name(write_csv):
    path = write_csv(HEADER + '"Коваль, Марія ""Мері""",КІ-302,4,88.4\n')
    assert load_records(path)[0]["full_name"] == 'Коваль, Марія "Мері"'


def test_text_is_normalized(write_csv):
    path = write_csv(HEADER + "  Іваненко   Олена , КІ-303 ,3,90\n")
    record = load_records(path)[0]
    assert record["full_name"] == "Іваненко Олена"
    assert record["group"] == "КІ-303"


def test_duplicates_are_kept(write_csv):
    row = "Олена,КІ-303,3,92.5\n"
    assert len(load_records(write_csv(HEADER + row + row))) == 2


def test_iter_records_is_lazy(write_csv):
    path = write_csv(HEADER + "Олена,КІ-303,3,90\nПетро,КІ-301,2,abc\n")
    iterator = iter_records(path)
    assert next(iterator)["full_name"] == "Олена"  # помилка ще не досягнута
    with pytest.raises(DataError):
        next(iterator)


def test_crlf_line_endings(write_csv):
    path = write_csv("full_name,group,year,average\r\nОлена,КІ-303,3,90\r\n")
    assert len(load_records(path)) == 1
