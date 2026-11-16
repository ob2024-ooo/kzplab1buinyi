import java.util.Locale;
import java.util.Objects;

/**
 * Спільний абстрактний тип запису реєстру студентів.
 *
 * <p>Базовий клас зберігає поля й інваріанти, які однакові для всіх підтипів
 * (ім'я, група, курс, середній бал, статус), тому підтипи їх не дублюють.
 * Відмінна поведінка (розрахунок рейтингу) оголошена абстрактною й
 * реалізується в підтипах. Об'єкт незмінний: сеттерів немає.
 *
 * <p>Обрано спадкування, бо {@code Bachelor} і {@code Master} є різновидами
 * студента (відношення «є різновидом»), а не частинами іншого об'єкта.
 */
public abstract class Learner {
    /** Найменший допустимий курс. */
    private static final int MIN_COURSE = 1;
    /** Найбільший допустимий курс. */
    private static final int MAX_COURSE = 6;
    /** Найменший допустимий середній бал. */
    private static final double MIN_AVERAGE = 0.0;
    /** Найбільший допустимий середній бал. */
    private static final double MAX_AVERAGE = 100.0;

    private final String name;
    private final String group;
    private final int course;
    private final double average;
    private final StudyStatus status;

    /**
     * Створює студента з перевіреними спільними полями.
     *
     * @param name ПІБ, не {@code null} і не порожнє
     * @param group назва групи, не {@code null} і не порожня
     * @param course курс від 1 до 6
     * @param average середній бал від 0 до 100 (скінченне число)
     * @param status академічний статус, не {@code null}
     * @throws NullPointerException якщо {@code name}, {@code group} або {@code status} дорівнює {@code null}
     * @throws IllegalArgumentException якщо порушено будь-який інваріант
     */
    protected Learner(String name, String group, int course,
                      double average, StudyStatus status) {
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
        Objects.requireNonNull(status, "Статус не може бути null");
        this.name = name.trim();
        this.group = group.trim();
        this.course = course;
        this.average = average;
        this.status = status;
    }

    /**
     * Перевіряє, що ім'я та група не порожні. Це правило використовують
     * конструктор і {@link LearnerFactory}.
     *
     * @param name ПІБ
     * @param group група
     * @throws IllegalArgumentException якщо ім'я або група порожні
     */
    static void requireText(String name, String group) {
        if (name.isBlank() || group.isBlank()) {
            throw new IllegalArgumentException("порожнє ім'я або група");
        }
    }

    /**
     * Обчислює рейтинг студента. Реалізація залежить від підтипу.
     *
     * @return рейтинг (середній бал з урахуванням правил підтипу й бонусу статусу)
     */
    public abstract double rating();

    /**
     * Повертає назву освітнього рівня для звіту.
     *
     * @return {@code "бакалавр"} або {@code "магістр"}
     */
    public abstract String degree();

    /** Повертає ПІБ студента. */
    public final String getName() {
        return name;
    }

    /** Повертає назву групи. */
    public final String getGroup() {
        return group;
    }

    /** Повертає курс. */
    public final int getCourse() {
        return course;
    }

    /** Повертає середній бал. */
    public final double getAverage() {
        return average;
    }

    /** Повертає академічний статус. */
    public final StudyStatus getStatus() {
        return status;
    }

    /** Повертає {@code true}, якщо студент отримує стипендію. */
    public final boolean isScholarship() {
        // enum порівнюється через ==, бо його значення є єдиними екземплярами.
        return status == StudyStatus.SCHOLARSHIP;
    }

    /**
     * Порівнює студентів: однаковий підтип, ім'я та група. Бал і статус
     * не входять до рівності, бо це дані, а не ідентифікатор студента.
     */
    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        // getClass() робить бакалавра й магістра з однаковим іменем нерівними.
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Learner that = (Learner) other;
        return name.equals(that.name) && group.equals(that.group);
    }

    /** Повертає хеш за тими самими полями, що й {@link #equals(Object)}. */
    @Override
    public final int hashCode() {
        return Objects.hash(name, group);
    }

    /** Повертає текстове подання студента для звіту й налагодження. */
    @Override
    public String toString() {
        // Locale.ROOT гарантує крапку як десятковий роздільник на будь-якій ОС.
        return String.format(Locale.ROOT, "%s (%s, %s, курс %d): бал %.2f, статус: %s",
                name, degree(), group, course, average, status.label());
    }
}
