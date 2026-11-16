# Реєстр студентів: Repository<T>, анотації та CSV (лабораторна №5, варіант 6)

## Призначення

П'ята версія консольної програми з лабораторних робіт №1-4. Додано параметризоване сховище `Repository<T>`, анотацію `@CsvColumn`, універсальний рефлексивний експортер `CsvExporter<T>`, імпорт CSV і власний перевірюваний виняток `CsvExportException` із причиною (`cause`). Звіти попередніх робіт не змінилися.

Рівень виконання: 1 (базовий).

## Модель CSV (варіант 6)

Клас `LearnerRecord`: `fullName`, `group`, `studyYear`, `average`.

| Поле Java | Тип | Стовпець CSV |
|---|---|---|
| `average` | `double` | `середній бал` |
| `fullName` | `String` | `ПІБ` |
| `group` | `String` | `група` |
| `studyYear` | `int` | `курс` |

Порядок стовпців визначається сортуванням Java-імен полів, тому заголовок завжди такий:

```text
середній бал,ПІБ,група,курс
```

Приклад рядка з екрануванням коми й лапок:

```text
88.4,"Коваль, Марія ""Мері""",КІ-302,4
```

Ключ round-trip: `fullName` + `group`; списки порівнюються через `equals`. Файли записуються й читаються в UTF-8, рядки розділяє `\n`.

## Збирання та запуск

macOS / Ubuntu:

```bash
javac -encoding UTF-8 -d out/classes src/main/java/*.java
java -cp out/classes Main
java -cp out/classes Main --input data/input-lab05.csv --export out/students.csv
java -cp out/classes Main --import out/students.csv
```

Windows (PowerShell):

```powershell
javac -encoding UTF-8 -d out\classes src\main\java\*.java
java -cp out\classes Main
java -cp out\classes Main --input data\input-lab05.csv --export out\students.csv
java -cp out\classes Main --import out\students.csv
```

Параметри: `--help`, `--input FILE`, `--output FILE`, `--ratings`, `--queries`, `--find ПІБ`, `--export FILE` (записати студентів у CSV, прочитати назад і порівняти списки), `--import FILE` (прочитати CSV і вивести записи). Помилки CSV виводяться з повідомленням і причиною.

## Перевірка збереження поведінки

```bash
java -cp out/classes Main --output out/report.txt
diff baseline/lab01-report.txt out/report.txt && echo "Звіт збігається"

java -cp out/classes Main --input data/edge-cases.csv --output out/edge-report.txt
diff baseline/lab02-edge-report.txt out/edge-report.txt && echo "Крайові випадки збігаються"

java -cp out/classes Main --input data/input-lab03.csv --ratings --output out/ratings.txt
diff baseline/lab03-ratings-report.txt out/ratings.txt && echo "Рейтинги збігаються"

java -cp out/classes Main --input data/input-lab05.csv --export out/students.csv --output out/report5.txt
diff baseline/lab05-export.csv out/students.csv && echo "CSV збігається"
```

## Перевірки

`checks/Lab05Check.java` (29 перевірок): сховище й незмінність знімка, анотація під час виконання, порядок і заголовок, екранування, інший доменний клас (`LocalDate`, enum), порожній набір, round-trip (6 записів, 8 фізичних рядків), порожній файл, хибний заголовок, хибна кількість полів, хибне число, хибне значення, незакриті лапки, помилки файла з перевіркою `getCause()`. `Lab03Check` і `Lab04Check` збережено.

macOS / Ubuntu:

```bash
javac -encoding UTF-8 -cp out/classes -d out/checks checks/Lab03Check.java checks/Lab04Check.java checks/Lab05Check.java
java -cp out/classes:out/checks Lab03Check
java -cp out/classes:out/checks Lab04Check
java -cp out/classes:out/checks Lab05Check
```

Windows (PowerShell, роздільник `;`):

```powershell
javac -encoding UTF-8 -cp out\classes -d out\checks checks\Lab03Check.java checks\Lab04Check.java checks\Lab05Check.java
java -cp "out\classes;out\checks" Lab03Check
java -cp "out\classes;out\checks" Lab04Check
java -cp "out\classes;out\checks" Lab05Check
```

Очікувані останні рядки: `Усі перевірки пройдено` (двічі) і `Перевірок: 29, помилок: 0`.

## Структура проєкту

```text
├── README.md
├── report5.md
├── .gitignore
├── baseline/
│   ├── lab01-input.csv
│   ├── lab01-report.txt
│   ├── lab02-edge-report.txt
│   ├── lab03-ratings-report.txt
│   └── lab05-export.csv
├── checks/
│   ├── Lab03Check.java
│   ├── Lab04Check.java
│   └── Lab05Check.java
├── data/
│   ├── input.csv
│   ├── input-lab03.csv
│   ├── input-lab04.csv
│   ├── input-lab05.csv
│   ├── edge-cases.csv
│   ├── empty-export.csv
│   └── bad-export.csv
└── src/main/java/
    ├── Repository.java
    ├── CsvColumn.java
    ├── CsvExporter.java
    ├── CsvParser.java
    ├── LearnerRecord.java
    ├── LearnerCsvImporter.java
    ├── CsvExportException.java
    └── (класи лабораторних №1-4: Learner, Bachelor, Master, StudyStatus,
        LearnerFactory, LearnerQueries, GradeScholarship, StudentParser,
        StudentMetrics, ReportFormatter, Main)
```

## Документація

Javadoc додано до нових класів і методів. Детальний опис наведено в `report5.md`.
