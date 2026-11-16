# Реєстр студентів: потокова обробка даних (лабораторна №4, варіант 6)

## Призначення

Четверта версія консольної програми з лабораторних робіт №1-3. Основні цикли обчислень замінено запитами Stream API в окремому класі `LearnerQueries`. Зовнішній формат звіту, вхідні дані та модель (`Learner`, `Bachelor`, `Master`, `StudyStatus`) не змінилися.

Рівень виконання: 1 (базовий), реалізовано всі п'ять запитів і пошук `Optional`.

## Запити варіанта 6

| № | Метод `LearnerQueries` | Операції | Результат |
|---|---|---|---|
| 1 | `excellent(list)` | `filter` (бал не нижче 90, межа включена) | `List<Learner>` |
| 2 | `names(list)` | `map` (ПІБ) | `List<String>` |
| 3 | `countByGroup(list)` | `groupingBy` + `counting` (ключ: група) | `Map<String, Long>` (`TreeMap`) |
| 4 | `scoreStatistics(list)` | `summarizingDouble` (бал) | `DoubleSummaryStatistics` |
| 5 | `top(list, n)` | `sorted` (бал спаданням, потім ПІБ) + `limit` | `List<Learner>` |
| 6 | `findByName(list, name)` | `filter` + `findFirst` | `Optional<Learner>` |

Усі методи працюють зі спільним типом `Learner` без `if` за підтипом, не змінюють вхідну колекцію та не форматують вивід. Форматування виконує `ReportFormatter`.

Показники звіту (`StudentMetrics`) тепер також обчислюються через потоки й дають ті самі значення, що й цикли.

## Поведінка на крайових даних

| Випадок | Поведінка |
|---|---|
| порожній список | порожні списки й карта; статистика має кількість 0; показники звіту дорівнюють 0 |
| порожня статистика | `getMax()` дає `-Infinity`, тому `StudentMetrics` перевіряє `getCount()` |
| `top(list, 0)` | порожній список |
| `top(list, n)` для завеликого `n` | усі елементи |
| `top(list, -1)` | `IllegalArgumentException` |
| ПІБ дублюється | `findByName` повертає перший за порядком |
| ПІБ відсутній або `null` | `Optional.empty()` |

## Формат вхідних даних

Без змін: файл у UTF-8, один запис у рядку, поля розділені `;`:

```text
name;group;course;average;scholarship
```

## Вимоги

JDK 21 (підійде 17 і новіша).

## Збирання та запуск

macOS / Ubuntu:

```bash
javac -encoding UTF-8 -d out/classes src/main/java/*.java
java -cp out/classes Main
java -cp out/classes Main --input data/input-lab04.csv --queries --find "Іваненко Олена"
```

Windows (PowerShell):

```powershell
javac -encoding UTF-8 -d out\classes src\main\java\*.java
java -cp out\classes Main
java -cp out\classes Main --input data\input-lab04.csv --queries --find "Іваненко Олена"
```

Параметри: `--help`, `--input FILE`, `--output FILE`, `--ratings` (рейтинги), `--queries` (розділ «Запити»), `--find ПІБ` (пошук, вмикає `--queries`).

## Перевірка збереження поведінки

Звіти попередніх робіт мають збігатися з еталонами в `baseline/`:

```bash
java -cp out/classes Main --output out/report.txt
diff baseline/lab01-report.txt out/report.txt && echo "Звіт збігається"

java -cp out/classes Main --input data/edge-cases.csv --output out/edge-report.txt
diff baseline/lab02-edge-report.txt out/edge-report.txt && echo "Крайові випадки збігаються"

java -cp out/classes Main --input data/input-lab03.csv --ratings --output out/ratings.txt
diff baseline/lab03-ratings-report.txt out/ratings.txt && echo "Рейтинги збігаються"
```

## Перевірка запитів

Програма `checks/Lab04Check.java` порівнює потокові запити з еталонними цикловими версіями та перевіряє порожній вхід, дублікати, межу фільтра, другий критерій компаратора, `N = 0`, завелике й від'ємне `N`, відсутній результат пошуку та незмінність вхідної колекції. `checks/Lab03Check.java` зберігає перевірки попередньої роботи (JUnit на рівні 1 не потрібен).

macOS / Ubuntu:

```bash
javac -encoding UTF-8 -cp out/classes -d out/checks checks/Lab03Check.java checks/Lab04Check.java
java -cp out/classes:out/checks Lab03Check
java -cp out/classes:out/checks Lab04Check
```

Windows (PowerShell, роздільник `;`):

```powershell
javac -encoding UTF-8 -cp out\classes -d out\checks checks\Lab03Check.java checks\Lab04Check.java
java -cp "out\classes;out\checks" Lab03Check
java -cp "out\classes;out\checks" Lab04Check
```

Очікуваний останній рядок обох програм: `Усі перевірки пройдено`.

## Структура проєкту

```text
├── README.md
├── report4.md
├── .gitignore
├── baseline/
│   ├── lab01-input.csv
│   ├── lab01-report.txt
│   ├── lab02-edge-report.txt
│   └── lab03-ratings-report.txt
├── checks/
│   ├── Lab03Check.java
│   └── Lab04Check.java
├── data/
│   ├── input.csv
│   ├── input-lab03.csv
│   ├── input-lab04.csv
│   └── edge-cases.csv
└── src/main/java/
    ├── Learner.java
    ├── Bachelor.java
    ├── Master.java
    ├── StudyStatus.java
    ├── LearnerFactory.java
    ├── LearnerQueries.java
    ├── GradeScholarship.java
    ├── StudentParser.java
    ├── StudentMetrics.java
    ├── ReportFormatter.java
    └── Main.java
```

## Документація

Javadoc додано до всіх публічних класів і методів, зокрема до `LearnerQueries`. Детальний опис наведено в `report4.md`.
