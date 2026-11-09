import java.util.Locale;
import java.util.Objects;

/**
 * Описує коректний запис реєстру студентів.
 *
 * <p>Стан об'єкта приховано в {@code private final}-полях і перевіряється в
 * конструкторі, тому некоректний студент не може потрапити до списку чи
 * статистики. Сеттерів немає: після створення об'єкт не змінюється.
 */
public final class Student {
    /** Найменший допустимий курс. */
    private static final int MIN_COURSE = 1;
    /** Найбільший допустимий курс (за правилами лабораторної роботи № 1). */
    private static final int MAX_COURSE = 6;
    /** Найменший допустимий середній бал. */
    private static final double MIN_AVERAGE = 0.0;
    /** Найбільший допустимий середній бал. */
    private static final double MAX_AVERAGE = 100.0;

    private final String name;
    private final String group;
    private final int course;
    private final double average;
    private final boolean scholarship;

    /**
     * Створює коректний запис студента.
     *
     * @param name ПІБ студента, не {@code null} і не порожнє
     * @param group назва групи, не {@code null} і не порожня
     * @param course курс від 1 до 6
     * @param average середній бал від 0 до 100 (скінченне число)
     * @param scholarship {@code true}, якщо студент отримує стипендію
     * @throws NullPointerException якщо {@code name} або {@code group} дорівнює {@code null}
     * @throws IllegalArgumentException якщо порушено будь-який інваріант
     */
    public Student(String name, String group, int course,
                   double average, boolean scholarship) {
        // null, порожній текст і числа поза межами мають різні причини відхилення.
        Objects.requireNonNull(name, "Ім'я не може бути null");
        Objects.requireNonNull(group, "Група не може бути null");
        requireText(name, group);
        if (course < MIN_COURSE || course > MAX_COURSE) {
            throw new IllegalArgumentException("курс має бути від 1 до 6");
        }
        // Double.isNaN відсікає NaN, який не порівнюється з жодним числом.
        if (Double.isNaN(average) || average < MIN_AVERAGE || average > MAX_AVERAGE) {
            throw new IllegalArgumentException("середній бал має бути від 0 до 100");
        }
        this.name = name.trim();
        this.group = group.trim();
        this.course = course;
        this.average = average;
        this.scholarship = scholarship;
    }

    /**
     * Створює студента з рядка CSV: {@code name;group;course;average;scholarship}.
     *
     * <p>Фабрика відповідає лише за структуру рядка й типи полів, а правила
     * значень перевіряє конструктор. Тому обійти перевірку не можна.
     *
     * @param line рядок CSV, не {@code null}
     * @return створений студент
     * @throws IllegalArgumentException якщо рядок порожній, має неправильну
     *         кількість полів, помилковий числовий формат або порушує інваріант
     */
    public static Student fromCsv(String line) {
        Objects.requireNonNull(line, "Рядок не може бути null");
        if (line.isBlank()) {
            throw new IllegalArgumentException("порожній рядок");
        }
        // -1 зберігає порожнє останнє поле, тому кількість полів перевіряється точно.
        String[] fields = line.split(";", -1);
        if (fields.length != 5) {
            throw new IllegalArgumentException(
                    "очікується 5 полів, знайдено %d".formatted(fields.length));
        }
        // Порядок перевірок такий самий, як у лабораторній роботі № 1.
        requireText(fields[0], fields[1]);
        String flag = fields[4].trim();
        if (!"true".equalsIgnoreCase(flag) && !"false".equalsIgnoreCase(flag)) {
            throw new IllegalArgumentException(
                    "поле scholarship має бути true або false");
        }
        try {
            int course = Integer.parseInt(fields[2].trim());
            double average = Double.parseDouble(fields[3].trim());
            return new Student(fields[0], fields[1], course, average,
                    Boolean.parseBoolean(flag));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "курс або бал має помилковий числовий формат", exception);
        }
    }

    /**
     * Перевіряє, що ім'я та група не порожні. Єдине місце цього правила
     * для конструктора і фабрики.
     */
    private static void requireText(String name, String group) {
        if (name.isBlank() || group.isBlank()) {
            throw new IllegalArgumentException("порожнє ім'я або група");
        }
    }

    /** Повертає ПІБ студента. */
    public String getName() {
        return name;
    }

    /** Повертає назву групи. */
    public String getGroup() {
        return group;
    }

    /** Повертає курс. */
    public int getCourse() {
        return course;
    }

    /** Повертає середній бал. */
    public double getAverage() {
        return average;
    }

    /** Повертає {@code true}, якщо студент отримує стипендію. */
    public boolean isScholarship() {
        return scholarship;
    }

    /** Повертає текстове подання студента для звіту й налагодження. */
    @Override
    public String toString() {
        // Locale.ROOT гарантує крапку як десятковий роздільник на будь-якій ОС.
        return String.format(Locale.ROOT, "%s (%s, курс %d): бал %.2f, стипендія: %s",
                name, group, course, average, scholarship ? "так" : "ні");
    }
}
