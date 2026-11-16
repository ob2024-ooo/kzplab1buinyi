# Реєстр студентів: обробка даних засобами Python (лабораторна №6, варіант 6)

## Призначення

Python-пакет `kzp_lab06` читає CSV реєстру студентів, перетворює рядки на типізовані записи, будує звіт за списками, словниками й множинами та записує його у текстовий файл. Є CLI, тести `pytest`, перевірка `ruff`, збирання wheel і GitHub Actions на Ubuntu, Windows та macOS.

Рівень виконання: 3 (повний цикл) разом із завданням із зірочкою (потокова обробка).

## Варіант 6

| Параметр | Значення |
|---|---|
| Поля CSV | `full_name`, `group`, `year`, `average` |
| Відбір | `average >= 90` (змінюється опцією `--threshold`) |
| Словник | кількість відмінників за `group` |
| Множина | унікальні групи |
| Звіт | кількість записів і груп, відмінники, середній бал, найкращий результат |

## Формат CSV

```csv
full_name,group,year,average
Іваненко Олена,КІ-303,3,92.5
"Коваль, Марія ""Мері""",КІ-302,4,88.4
```

UTF-8, перший рядок — заголовок. `year` — ціле від 1 до 6, `average` — число від 0 до 100. Кома, лапки й перенесення рядка в ПІБ екранує сам модуль `csv`. Файл лише із заголовком дає порожній коректний звіт; файл без заголовка, відсутній стовпець, порожнє або хибне значення дають `DataError` з номером рядка.

## Структура

```text
├── pyproject.toml
├── README.md
├── report6.md
├── .gitignore
├── .github/workflows/ci.yml
├── baseline/summary.txt        # еталонний звіт
├── data/                       # input.csv, empty.csv, broken.csv, missing-column.csv
├── reports/                    # вихідні звіти (не додаються до репозиторію)
├── scripts/                    # verify_wheel.py, measure_memory.py
├── src/kzp_lab06/
│   ├── __init__.py
│   ├── errors.py               # DataError
│   ├── loader.py               # читання CSV, перетворення типів
│   ├── analysis.py             # відбір, словник, множина, summarize, потоковий підсумок
│   ├── reporting.py            # формат і запис звіту
│   └── cli.py                  # аргументи й коди завершення
└── tests/                      # 53 тести pytest
```

## Запуск

macOS / Ubuntu:

```bash
python3 -m venv .venv
source .venv/bin/activate
python -m pip install -e ".[dev]"
python -m kzp_lab06.cli data/input.csv reports/summary.txt
```

Windows (PowerShell):

```powershell
python -m venv .venv
.venv\Scripts\activate
python -m pip install -e ".[dev]"
python -m kzp_lab06.cli data\input.csv reports\summary.txt
```

Після встановлення доступна команда `kzp-lab06 data/input.csv reports/summary.txt`. Опції: `--threshold N` (межа відмінника), `--stream` (потокова обробка без збереження записів у пам'яті).

Коди завершення: `0` — успіх, `1` — помилка даних (`DataError`), `2` — помилка аргументів, `3` — помилка файла, `4` — неочікувана помилка.

## Перевірка

```bash
python -m pytest
ruff check .
ruff format --check .
python -m build
python scripts/verify_wheel.py
diff baseline/summary.txt reports/wheel-summary.txt && echo "Звіт збігається"
```

`scripts/verify_wheel.py` створює чисте середовище `.venv-wheel`, встановлює wheel із `dist/` без залежностей розробки (`--no-deps`), запускає CLI і порівнює звіт з еталоном. У PowerShell, якщо активація заборонена політикою, можна запускати `.venv\Scripts\python.exe` без активації.

## Приклад звіту

```text
Реєстр студентів: звіт
Записів: 8
Груп: 3
Відмінників (середній бал >= 90.0): 4
Відмінники за групами:
КІ-301: 1
КІ-303: 3
Середній бал: 84.46
Найкращий результат: Лисенко Ірина (КІ-303), 95.10
```

## Потокова обробка (зірочка)

`iter_records` — генератор, `stream_summary` обчислює підсумок за один прохід. Порівняння на 200 000 рядків: `python scripts/measure_memory.py`.

## CI

`.github/workflows/ci.yml` на Ubuntu, Windows та macOS запускає `pytest`, `ruff check`, `ruff format --check`, `python -m build`, перевірку wheel у чистому середовищі, успішний і помилковий сценарії CLI та публікує wheel як артефакт `wheel-<ОС>`.

## Документація

Усі модулі й функції мають docstrings. Детальний опис наведено в `report6.md`.
