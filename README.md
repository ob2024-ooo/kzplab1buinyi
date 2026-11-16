# Реєстр студентів: спадкування, enum і поліморфізм (лабораторна №3, варіант 6)

## Призначення

Третя версія консольної програми з лабораторних робіт №1 і №2. Запис про студента тепер описує абстрактний базовий тип `Learner` з двома підтипами: `Bachelor` (бакалавр) і `Master` (магістр). Академічний статус задає enum `StudyStatus`. Розрахунок рейтингу реалізовано поліморфним методом `rating()`. Формат вхідних даних і звіт для основного файла не змінилися.

Рівень виконання: 1 (базовий).

## Що змінилося порівняно з лабораторною №2

| Стан | Лабораторна №2 | Лабораторна №3 |
|---|---|---|
| Запис студента | `final class Student` | абстрактний `Learner` і підтипи `Bachelor`, `Master` |
| Відмінна поведінка | немає | абстрактні `rating()` і `degree()`, перевизначені з `@Override` |
| Категорія | прапорець `boolean scholarship` | enum `StudyStatus` (`SCHOLARSHIP`, `REGULAR`) з підписом і бонусом |
| Створення з рядка | `Student.fromCsv` | `LearnerFactory.fromCsv` (підтип обирається за курсом) |
| Рівність | не визначена | `equals` і `hashCode` за підтипом, ім'ям і групою |
| Звіт | чотири показники | ті самі чотири показники; додатково `--ratings` |

## Ієрархія

```text
Learner (abstract)          спільні поля й інваріанти, rating(), degree()
├── Bachelor   курси 1-4    rating = середній бал + бонус статусу
└── Master     курси 5-6    rating = середній бал * 1.1 + бонус статусу
StudyStatus (enum)          SCHOLARSHIP (+5.0), REGULAR (+0.0)
```

Обрано спадкування, бо бакалавр і магістр є різновидами студента («є різновидом»), мають спільні поля та правила й відрізняються лише поведінкою. Композиція була б зручнішою, якби поведінка змінювалася під час життя об'єкта.

## Правила перевірки

Спільні інваріанти перевіряє конструктор `Learner` (їх немає в підтипах):

| Поле | Правило |
|---|---|
| `name`, `group` | не `null` і не порожні |
| `course` | ціле від 1 до 6 |
| `average` | число від 0 до 100, не `NaN` |
| `status` | не `null` |

Підтипи додатково перевіряють курс: `Bachelor` від 1 до 4, `Master` від 5 до 6.

## Рівність

Два студенти рівні, якщо вони одного підтипу й мають однакові ім'я та групу. Бал і статус у рівність не входять. Бакалавр і магістр з однаковими іменем та групою не рівні. `hashCode` обчислюється за тими самими полями (ім'я й група).

## Формат вхідних даних

Файл у UTF-8, один запис у рядку, поля розділені `;` (як і раніше):

```text
name;group;course;average;scholarship
```

Підтип визначається курсом: 1-4 дає `Bachelor`, 5-6 дає `Master`.

## Вимоги

JDK 21 (підійде 17 і новіша).

## Збирання та запуск

macOS / Ubuntu:

```bash
javac -encoding UTF-8 -d out/classes src/main/java/*.java
java -cp out/classes Main
java -cp out/classes Main --input data/input-lab03.csv --ratings
```

Windows (PowerShell):

```powershell
javac -encoding UTF-8 -d out\classes src\main\java\*.java
java -cp out\classes Main
java -cp out\classes Main --input data\input-lab03.csv --ratings
```

Параметри: `--help`, `--input FILE` (за замовчуванням `data/input.csv`), `--output FILE` (за замовчуванням `out/report.txt`), `--ratings` (додати розділ «Рейтинги»).

## Перевірка збереження поведінки

Звіт для `data/input.csv` має збігатися з виводом лабораторної №1, а крайові випадки з виводом лабораторної №2:

```bash
java -cp out/classes Main --output out/report.txt
diff baseline/lab01-report.txt out/report.txt && echo "Звіт збігається"

java -cp out/classes Main --input data/edge-cases.csv --output out/edge-report.txt
diff baseline/lab02-edge-report.txt out/edge-report.txt && echo "Крайові випадки збігаються"
```

## Перевірка нової моделі

Програма `checks/Lab03Check.java` перевіряє підтипи, поліморфний виклик, enum, рівність у `HashSet` і негативні випадки (JUnit на рівні 1 не потрібен).

macOS / Ubuntu:

```bash
javac -encoding UTF-8 -cp out/classes -d out/checks checks/Lab03Check.java
java -cp out/classes:out/checks Lab03Check
```

Windows (PowerShell, роздільник `;`):

```powershell
javac -encoding UTF-8 -cp out\classes -d out\checks checks\Lab03Check.java
java -cp "out\classes;out\checks" Lab03Check
```

Очікуваний останній рядок: `Усі перевірки пройдено`.

## Приклад виводу з рейтингами

```text
Лабораторна робота №1, варіант 6
Коректних записів: 5
Середній бал: 87.78
Найбільший бал: 95.00
Кількість стипендіатів: 3
Пропущено рядків: 2
Рядок 6: середній бал має бути від 0 до 100
Рядок 7: очікується 5 полів, знайдено 4
Рейтинги:
Іваненко Олена (бакалавр, стипендіат): 97.50
Петренко Андрій (бакалавр, без стипендії): 78.00
Коваль Марія (бакалавр, стипендіат): 93.40
Мельник Дмитро (магістр, без стипендії): 93.50
Савчук Ірина (магістр, стипендіат): 109.50
```

## Структура проєкту

```text
├── README.md
├── report3.md
├── .gitignore
├── baseline/
│   ├── lab01-input.csv
│   ├── lab01-report.txt
│   └── lab02-edge-report.txt
├── checks/
│   └── Lab03Check.java
├── data/
│   ├── input.csv
│   ├── input-lab03.csv
│   └── edge-cases.csv
└── src/main/java/
    ├── Learner.java
    ├── Bachelor.java
    ├── Master.java
    ├── StudyStatus.java
    ├── LearnerFactory.java
    ├── GradeScholarship.java
    ├── StudentParser.java
    ├── StudentMetrics.java
    ├── ReportFormatter.java
    └── Main.java
```

## Документація

Javadoc додано до всіх публічних класів, конструкторів, методів і enum. Детальний опис наведено в `report3.md`.
