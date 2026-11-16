# Реєстр студентів: об'єктна обробка даних у Python (лабораторна №7, варіант 6)

## Призначення

Продовження лабораторної №6: словникові записи замінено об'єктами з власною поведінкою. Формат CSV, варіант і зовнішній звіт не змінилися; змінився спосіб зберігання стану й вибору поведінки.

Рівень виконання: 3 (повний цикл) разом із завданнями із зірочкою (`Protocol` із властивостями й методами, власний контекстний менеджер, порівняння пам'яті).

## Модель варіанта 6

| Елемент | Опис |
|---|---|
| `Learner` | абстрактна базова сутність: `full_name`, `group`, `year`, `average` як `@property` із setter-перевіркою; `__str__`, `__repr__`, `__eq__` |
| `Bachelor` | курси 1-4; `rating() = average * 1.0 + year * 0.5` |
| `Master` | курси 5-6; `rating() = average * 1.1 + (year - 4) * 2.0` |
| `Rated` | `Protocol` (властивості `full_name`, `group`, `average` і метод `rating()`); `analysis` працює лише з ним |
| `LearnerSummary`, `GroupRating` | `@dataclass(frozen=True)`: підсумок реєстру й рейтинг групи |
| `ReportWriter` | контекстний менеджер запису звіту (закриває файл і при винятку) |

Підтип обирається один раз у `loader.build_learner` (курс 1-4 дає `Bachelor`, 5-6 дає `Master`). Модуль `analysis` не містить `isinstance`, назв підтипів і розгалужень за типом.

## Формат CSV

```csv
full_name,group,year,average
Іваненко Олена,КІ-303,3,92.5
"Коваль, Марія ""Мері""",КІ-302,4,88.4
```

UTF-8, `year` - ціле від 1 до 6, `average` - від 0 до 100. Помилки мають номер рядка й першопричину.

## Структура

```text
├── pyproject.toml
├── README.md
├── report7.md
├── .gitignore
├── .github/workflows/ci.yml
├── baseline/                   # summary.txt (звіт лаб. №6), ratings.txt
├── data/                       # input.csv, empty.csv, broken.csv, missing-column.csv
├── reports/                    # вихідні звіти (не додаються до репозиторію)
├── scripts/                    # verify_wheel.py, measure_memory.py
├── src/kzp_lab07/
│   ├── __init__.py
│   ├── errors.py               # DataError
│   ├── models.py               # Learner, Bachelor, Master, Rated, dataclass-и
│   ├── loader.py               # CSV -> об'єкти
│   ├── analysis.py             # обчислення через протокол
│   ├── reporting.py            # формат звіту, ReportWriter
│   └── cli.py                  # аргументи й коди завершення
└── tests/                      # 91 тест pytest
```

## Запуск

macOS / Ubuntu:

```bash
python3 -m venv .venv
source .venv/bin/activate
python -m pip install -e ".[dev]"
python -m kzp_lab07.cli data/input.csv reports/summary.txt
```

Windows (PowerShell):

```powershell
python -m venv .venv
.venv\Scripts\activate
python -m pip install -e ".[dev]"
python -m kzp_lab07.cli data\input.csv reports\summary.txt
```

Після встановлення доступна команда `kzp-lab07 data/input.csv reports/summary.txt`. Опції: `--threshold N` (межа відмінника), `--ratings` (додати рейтинг груп). Коди завершення: `0` успіх, `1` помилка даних, `2` помилка аргументів, `3` помилка файла, `4` неочікувана помилка.

## Перевірка

```bash
python -m pytest
ruff check .
ruff format --check .
python -m build
python scripts/verify_wheel.py
python scripts/measure_memory.py
```

`verify_wheel.py` встановлює wheel із `dist/` у чисте середовище без залежностей розробки, запускає CLI й порівнює звіт з `baseline/summary.txt` (звіт лабораторної №6). Звіт без `--ratings` збігається з ним байт у байт.

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

З `--ratings` наприкінці додається:

```text
Рейтинг груп:
КІ-301: 82.23 (3)
КІ-302: 83.20 (2)
КІ-303: 98.20 (3)
```

## CI

`.github/workflows/ci.yml` на Ubuntu, Windows та macOS запускає `pytest`, `ruff check`, `ruff format --check`, `python -m build`, перевірку wheel у чистому середовищі, успішний і помилковий сценарії CLI та публікує wheel як артефакт `wheel-<ОС>`.

## Документація

Усі модулі, класи й методи мають docstrings. Детальний опис наведено в `report7.md`.
