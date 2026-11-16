import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Проста ручна перевірка моделі лабораторної роботи № 3 без JUnit.
 * Кожна перевірка друкує OK або FAIL; код завершення 1 означає помилку.
 */
public final class Lab03Check {
    private static int failures;

    /* Забороняє створення екземплярів службового класу. */
    private Lab03Check() {
    }

    /** Запускає всі перевірки. */
    public static void main(String[] args) {
        // Явне UTF-8 для консолі, як у Main.
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out),
                true, StandardCharsets.UTF_8));
        // Підтипи й поліморфний метод
        Learner b = new Bachelor("Іваненко Олена", "КІ-303", 3, 92.5, StudyStatus.SCHOLARSHIP);
        Learner m = new Master("Савчук Ірина", "КІм-61", 6, 95.0, StudyStatus.SCHOLARSHIP);
        check("рейтинг бакалавра = бал + бонус", near(b.rating(), 97.5));
        check("рейтинг магістра = бал * 1.1 + бонус", near(m.rating(), 109.5));
        check("degree() різний у підтипів",
                b.degree().equals("бакалавр") && m.degree().equals("магістр"));
        List<Learner> all = List.of(b, m);
        double sum = 0;
        for (Learner learner : all) {
            sum += learner.rating();
        }
        check("колекція спільного типу, поліморфний виклик", near(sum, 207.0));

        // enum
        check("StudyStatus.of(true) == SCHOLARSHIP", StudyStatus.of(true) == StudyStatus.SCHOLARSHIP);
        check("StudyStatus.of(false) == REGULAR", StudyStatus.of(false) == StudyStatus.REGULAR);
        check("enum має підпис і бонус",
                StudyStatus.SCHOLARSHIP.label().equals("стипендіат")
                        && near(StudyStatus.SCHOLARSHIP.bonus(), 5.0)
                        && near(StudyStatus.REGULAR.bonus(), 0.0));

        // Рівність у HashSet
        Set<Learner> set = new HashSet<>();
        set.add(new Bachelor("Коваль Марія", "КІ-302", 4, 88.4, StudyStatus.SCHOLARSHIP));
        set.add(new Bachelor("Коваль Марія", "КІ-302", 3, 70.0, StudyStatus.REGULAR));
        check("однакові ім'я й група: один елемент у HashSet", set.size() == 1);
        Learner x = new Bachelor("Тест Тестович", "КІ-301", 2, 80, StudyStatus.REGULAR);
        Learner y = new Master("Тест Тестович", "КІ-301", 5, 80, StudyStatus.REGULAR);
        check("різні підтипи з однаковими даними нерівні", !x.equals(y));
        check("equals симетричний і рефлексивний", x.equals(x) && !y.equals(x));
        Learner x2 = new Bachelor("Тест Тестович", "КІ-301", 1, 10, StudyStatus.SCHOLARSHIP);
        check("рівні об'єкти мають однаковий hashCode", x.equals(x2) && x.hashCode() == x2.hashCode());
        check("equals(null) == false", !x.equals(null));

        // Фабрика
        check("курс 4 дає Bachelor", LearnerFactory.fromCsv("А;Г;4;80;false") instanceof Bachelor);
        check("курс 5 дає Master", LearnerFactory.fromCsv("А;Г;5;80;false") instanceof Master);

        // Негативні випадки
        rejects("бакалавр із курсом 5", () -> new Bachelor("А", "Г", 5, 80, StudyStatus.REGULAR));
        rejects("магістр із курсом 4", () -> new Master("А", "Г", 4, 80, StudyStatus.REGULAR));
        rejects("порожнє ім'я", () -> new Bachelor(" ", "Г", 1, 80, StudyStatus.REGULAR));
        rejects("курс 0", () -> new Bachelor("А", "Г", 0, 80, StudyStatus.REGULAR));
        rejects("курс 7", () -> new Master("А", "Г", 7, 80, StudyStatus.REGULAR));
        rejects("бал 100.01", () -> new Bachelor("А", "Г", 1, 100.01, StudyStatus.REGULAR));
        rejects("бал NaN", () -> new Bachelor("А", "Г", 1, Double.NaN, StudyStatus.REGULAR));
        rejects("статус null", () -> new Bachelor("А", "Г", 1, 80, null));
        rejects("рядок із 4 полями", () -> LearnerFactory.fromCsv("А;Г;1;80"));
        rejects("нечисловий курс", () -> LearnerFactory.fromCsv("А;Г;abc;80;true"));

        System.out.println(failures == 0 ? "Усі перевірки пройдено" : "Помилок: " + failures);
        System.exit(failures == 0 ? 0 : 1);
    }

    /* Порівнює числа з допуском для double. */
    private static boolean near(double a, double b) {
        return Math.abs(a - b) < 1e-9;
    }

    /* Друкує результат однієї перевірки. */
    private static void check(String name, boolean condition) {
        if (!condition) {
            failures++;
        }
        System.out.println((condition ? "OK   " : "FAIL ") + name);
    }

    /* Перевіряє, що дія кидає виняток типу IllegalArgumentException або NullPointerException. */
    private static void rejects(String name, Runnable action) {
        boolean thrown = false;
        try {
            action.run();
        } catch (IllegalArgumentException | NullPointerException expected) {
            thrown = true;
        }
        check("відхиляє: " + name, thrown);
    }
}
