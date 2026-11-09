# Реєстр студентів: класи, record та інкапсуляція (лабораторна №2, варіант 6)

## Призначення

Друга версія консольної програми з лабораторної роботи № 1. Рядки й паралельні змінні замінено власними класами: запис про студента описує клас `Student`, підсумок показників зберігає `record GradeScholarship`. Зовнішня поведінка не змінилася: програма читає ті самі дані, відкидає ті самі хибні рядки й виводить такий самий звіт.

Рівень виконання: 1 (базовий).

## Що змінилося порівняно з лабораторною № 1

| Стан | Лабораторна № 1 | Лабораторна № 2 |
|---|---|---|
| Один запис | `record Student` без перевірки | `final class Student` з `private final` полями |
| Перевірка | умови в циклі `StudentParser` | конструктор `Student` і фабрика `Student.fromCsv` |
| Набір даних | `List<Student>` | `List<Student>` (тепер усі об'єкти коректні за побудовою) |
| Допоміжний результат | чотири окремі змінні | `record GradeScholarship` |
| Звіт | текст із чотирьох показників | той самий текст (порівняно побайтово) |

## Клас-сутність `Student`

Поля: `name`, `group`, `course`, `average`, `scholarship`. Усі `private final`, сеттерів немає.

Інваріанти (перевіряє конструктор):

| Поле | Правило |
|---|---|
| `name`, `group` | не `null` і не порожні |
| `course` | ціле від 1 до 6 |
| `average` | число від 0 до 100, не `NaN` |
| `scholarship` | `true` або `false` (розбирає `fromCsv`) |

Методи: `getName()`, `getGroup()`, `getCourse()`, `getAverage()`, `isScholarship()`, `toString()` (формат із `Locale.ROOT`), статична фабрика `fromCsv(String line)`.

`fromCsv` відповідає за структуру рядка (5 полів, типи), конструктор за значення. Номер рядка в повідомленні додає цикл читання в `StudentParser`.

## record `GradeScholarship`

Незмінні підсумки варіанта 6: `count`, `averageScore`, `maxScore`, `scholarshipCount`. Компактний конструктор перевіряє, що кількість не від'ємна, стипендіатів не більше, ніж записів, а бали в діапазоні 0-100.

## Формат вхідних даних

Файл у UTF-8, один запис у рядку, поля розділені `;` (як у лабораторній № 1):

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
```

Windows (PowerShell):

```powershell
javac -encoding UTF-8 -d out\classes src\main\java\*.java
java -cp out\classes Main
```

Параметри: `--help`, `--input FILE` (за замовчуванням `data/input.csv`), `--output FILE` (за замовчуванням `out/report.txt`).

## Перевірка збереження поведінки

Файл `baseline/lab01-report.txt` містить вивід програми лабораторної № 1 на тих самих даних. Після перебудови виводи мають збігатися:

```bash
java -cp out/classes Main --output out/report.txt
diff baseline/lab01-report.txt out/report.txt && echo "Звіт збігається"
```

Додаткові крайові випадки лежать у `data/edge-cases.csv`:

```bash
java -cp out/classes Main --input data/edge-cases.csv --output out/edge-report.txt
```

## Приклад виводу

```text
Лабораторна робота №1, варіант 6
Коректних записів: 3
Середній бал: 86.30
Найбільший бал: 92.50
Кількість стипендіатів: 2
Пропущено рядків: 4
Рядок 4: середній бал має бути від 0 до 100
Рядок 5: курс або бал має помилковий числовий формат
Рядок 6: порожнє ім'я або група
Рядок 7: очікується 5 полів, знайдено 4
```

## Структура проєкту

```text
├── README.md
├── REPORT.md
├── .gitignore
├── baseline/
│   ├── lab01-input.csv
│   └── lab01-report.txt
├── data/
│   ├── input.csv
│   └── edge-cases.csv
└── src/main/java/
    ├── Student.java
    ├── GradeScholarship.java
    ├── StudentParser.java
    ├── StudentMetrics.java
    ├── ReportFormatter.java
    └── Main.java
```

## Документація

Javadoc додано до класів, конструкторів, фабричного методу, методів читання та `record`. Детальний опис наведено в `REPORT.md`.
