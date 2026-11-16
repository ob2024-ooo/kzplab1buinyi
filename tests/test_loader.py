"""Тести завантаження: CSV -> об'єкти, помилки з номером рядка."""

import pytest

from kzp_lab07.errors import DataError
from kzp_lab07.loader import build_learner, load_learners
from kzp_lab07.models import Bachelor, Master

HEADER = "full_name,group,year,average\n"


def test_load_valid_rows_creates_subtypes(write_csv):
    path = write_csv(HEADER + "Олена,КІ-303,3,92.5\nІрина,КІ-303,5,95.1\n")
    learners = load_learners(path)
    assert [type(x) for x in learners] == [Bachelor, Master]
    assert learners[0] == Bachelor("Олена", "КІ-303", 3, 92.5)


def test_header_only_gives_empty_list(write_csv):
    assert load_learners(write_csv(HEADER)) == []


def test_completely_empty_file_is_error(write_csv):
    with pytest.raises(DataError, match="немає заголовка"):
        load_learners(write_csv(""))


def test_missing_column(write_csv):
    with pytest.raises(DataError, match="average"):
        load_learners(write_csv("full_name,group,year\nОлена,КІ-303,3\n"))


def test_missing_value_in_row(write_csv):
    with pytest.raises(DataError, match="Рядок 2: поле average порожнє"):
        load_learners(write_csv(HEADER + "Олена,КІ-303,3\n"))


def test_extra_fields_in_row(write_csv):
    with pytest.raises(DataError, match="забагато полів"):
        load_learners(write_csv(HEADER + "Олена,КІ-303,3,90,зайве\n"))


def test_bad_number_keeps_cause_and_row_number(write_csv):
    path = write_csv(HEADER + "Олена,КІ-303,3,90\nПетро,КІ-301,2,abc\n")
    with pytest.raises(
        DataError, match="Рядок 3: поле average має бути числом"
    ) as info:
        load_learners(path)
    assert isinstance(info.value.__cause__, ValueError)


def test_bad_year_text(write_csv):
    with pytest.raises(DataError, match="year має бути цілим") as info:
        load_learners(write_csv(HEADER + "Олена,КІ-303,x,90\n"))
    assert isinstance(info.value.__cause__, ValueError)


@pytest.mark.parametrize("year", ["0", "7", "-1"])
def test_year_out_of_range(year):
    row = {"full_name": "О", "group": "Г", "year": year, "average": "50"}
    with pytest.raises(DataError, match="Рядок 2: поле year має бути від 1 до 6"):
        build_learner(row, 2)


@pytest.mark.parametrize("average", ["-0.1", "100.1", "nan"])
def test_average_out_of_range(average):
    row = {"full_name": "О", "group": "Г", "year": "1", "average": average}
    with pytest.raises(DataError, match="від 0 до 100"):
        build_learner(row, 2)


@pytest.mark.parametrize(
    ("year", "kind"), [("1", Bachelor), ("4", Bachelor), ("5", Master), ("6", Master)]
)
def test_year_boundaries_choose_subtype(year, kind):
    row = {"full_name": "О", "group": "Г", "year": year, "average": "0"}
    assert type(build_learner(row, 2)) is kind


def test_comma_and_quotes_in_name(write_csv):
    path = write_csv(HEADER + '"Коваль, Марія ""Мері""",КІ-302,4,88.4\n')
    assert load_learners(path)[0].full_name == 'Коваль, Марія "Мері"'


def test_duplicates_are_kept_and_equal(write_csv):
    row = "Олена,КІ-303,3,92.5\n"
    learners = load_learners(write_csv(HEADER + row + row))
    assert len(learners) == 2
    assert learners[0] == learners[1]


def test_crlf_line_endings(write_csv):
    path = write_csv("full_name,group,year,average\r\nОлена,КІ-303,3,90\r\n")
    assert len(load_learners(path)) == 1
