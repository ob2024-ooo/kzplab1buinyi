import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Ручна перевірка запитів Stream API лабораторної роботи № 4 без JUnit.
 * Результати потокових запитів порівнюються з еталонними цикловими версіями.
 * Код завершення 1 означає помилку.
 */
public final class Lab04Check {
    private static int failures;

    /* Забороняє створення екземплярів службового класу. */
    private Lab04Check() {
    }

    /** Запускає всі перевірки. */
    public static void main(String[] args) {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out),
                true, StandardCharsets.UTF_8));
        List<String> lines = List.of(
                "Іваненко Олена;КІ-303;3;92.5;true",
                "Петренко Андрій;КІ-301;2;78.0;false",
                "Коваль Марія;КІ-302;4;88.4;true",
                "Мельник Дмитро;КІм-51;5;85.0;false",
                "Савчук Ірина;КІм-61;6;95.0;true",
                "Бондар Олег;КІ-301;1;90.0;false",
                "Андрієнко Богдан;КІ-302;3;88.4;false",
                "Іваненко Олена;КІ-301;2;60.0;false",
                "Шевченко Ігор;КІ-303;3;-5;false",
                "Бондар Петро;КІ-302;2;75");
        StudentParser.ParseResult parsed = StudentParser.parse(lines);
        List<Learner> data = parsed.students();
        List<Learner> empty = List.of();
        check("розібрано 8 коректних і 2 хибні рядки",
                data.size() == 8 && parsed.errors().size() == 2);

        // Запит 1: filter
        List<Learner> excellent = LearnerQueries.excellent(data);
        check("filter: межа 90.0 включена, результат збігається з циклом",
                excellent.size() == 3 && excellent.equals(excellentLoop(data)));
        check("filter: порожній вхід дає порожній результат",
                LearnerQueries.excellent(empty).isEmpty());

        // Запит 2: map
        check("map: ПІБ збігаються з циклом, дублікат збережено",
                LearnerQueries.names(data).equals(namesLoop(data))
                        && Collections.frequency(LearnerQueries.names(data), "Іваненко Олена") == 2);
        check("map: порожній вхід", LearnerQueries.names(empty).isEmpty());

        // Запит 3: groupingBy
        Map<String, Long> groups = LearnerQueries.countByGroup(data);
        check("groupingBy: КІ-301=3, КІ-302=2, КІ-303=1",
                groups.get("КІ-301") == 3L && groups.get("КІ-302") == 2L && groups.get("КІ-303") == 1L);
        check("groupingBy: збігається з циклом", groups.equals(countByGroupLoop(data)));
        check("groupingBy: порожній вхід дає порожню карту", LearnerQueries.countByGroup(empty).isEmpty());

        // Запит 4: статистика
        DoubleSummaryStatistics stats = LearnerQueries.scoreStatistics(data);
        check("статистика: кількість, мінімум, максимум",
                stats.getCount() == 8 && near(stats.getMin(), 60.0) && near(stats.getMax(), 95.0));
        check("статистика: середнє збігається з циклом",
                near(stats.getAverage(), averageLoop(data)));
        check("статистика: порожній вхід (кількість 0, середнє 0)",
                LearnerQueries.scoreStatistics(empty).getCount() == 0
                        && near(LearnerQueries.scoreStatistics(empty).getAverage(), 0.0));
        check("показники звіту для порожнього входу дорівнюють 0",
                near(StudentMetrics.averageScore(empty), 0.0)
                        && near(StudentMetrics.maxScore(empty), 0.0)
                        && StudentMetrics.countScholarship(empty) == 0);
        check("показники звіту збігаються з циклами",
                near(StudentMetrics.averageScore(data), averageLoop(data))
                        && near(StudentMetrics.maxScore(data), 95.0)
                        && StudentMetrics.countScholarship(data) == 3);

        // Запит 5: top-N зі складеним компаратором
        List<Learner> top = LearnerQueries.top(data, 5);
        check("top-5: збігається з цикловим сортуванням", top.equals(topLoop(data, 5)));
        check("top-5: порядок Савчук, Іваненко, Бондар Олег, Андрієнко, Коваль",
                top.size() == 5 && top.get(0).getName().equals("Савчук Ірина")
                        && top.get(1).getName().equals("Іваненко Олена")
                        && top.get(2).getName().equals("Бондар Олег")
                        && top.get(3).getName().equals("Андрієнко Богдан")
                        && top.get(4).getName().equals("Коваль Марія"));
        check("top: другий критерій (ПІБ) вирішує нічию 88.4",
                top.get(3).getAverage() == top.get(4).getAverage());
        check("top: шостий запис усічено", !top.contains(data.get(3)));
        check("top: N = 0 дає порожній список", LearnerQueries.top(data, 0).isEmpty());
        check("top: завелике N дає всіх", LearnerQueries.top(data, 100).size() == 8);
        check("top: порожній вхід", LearnerQueries.top(empty, 5).isEmpty());
        rejects("top: від'ємне N", () -> LearnerQueries.top(data, -1));

        // Optional-пошук
        Optional<Learner> first = LearnerQueries.findByName(data, "Іваненко Олена");
        check("Optional: знайдено перший із дублікатів", first.isPresent() && first.get() == data.get(0));
        check("Optional: відсутній запис дає порожній результат",
                LearnerQueries.findByName(data, "Невідомий Студент").isEmpty());
        check("Optional: null і порожній вхід", LearnerQueries.findByName(data, null).isEmpty()
                && LearnerQueries.findByName(empty, "Іваненко Олена").isEmpty());

        // Запити не змінюють вхідну колекцію
        List<Learner> before = new ArrayList<>(data);
        LearnerQueries.excellent(data);
        LearnerQueries.names(data);
        LearnerQueries.countByGroup(data);
        LearnerQueries.scoreStatistics(data);
        LearnerQueries.top(data, 5);
        check("вхідна колекція не змінилася", data.equals(before));
        rejects("результат запиту незмінний", () -> LearnerQueries.excellent(data).add(data.get(0)));

        System.out.println(failures == 0 ? "Усі перевірки пройдено" : "Помилок: " + failures);
        System.exit(failures == 0 ? 0 : 1);
    }

    // --- Еталонні циклові реалізації (лише для порівняння) ---

    private static List<Learner> excellentLoop(List<Learner> learners) {
        List<Learner> result = new ArrayList<>();
        for (Learner learner : learners) {
            if (learner.getAverage() >= 90.0) {
                result.add(learner);
            }
        }
        return result;
    }

    private static List<String> namesLoop(List<Learner> learners) {
        List<String> result = new ArrayList<>();
        for (Learner learner : learners) {
            result.add(learner.getName());
        }
        return result;
    }

    private static Map<String, Long> countByGroupLoop(List<Learner> learners) {
        Map<String, Long> result = new TreeMap<>();
        for (Learner learner : learners) {
            result.merge(learner.getGroup(), 1L, Long::sum);
        }
        return result;
    }

    private static double averageLoop(List<Learner> learners) {
        double sum = 0.0;
        for (Learner learner : learners) {
            sum += learner.getAverage();
        }
        return learners.isEmpty() ? 0.0 : sum / learners.size();
    }

    private static List<Learner> topLoop(List<Learner> learners, int n) {
        List<Learner> copy = new ArrayList<>(learners);
        Comparator<Learner> order = (a, b) -> {
            int byScore = Double.compare(b.getAverage(), a.getAverage());
            return byScore != 0 ? byScore : a.getName().compareTo(b.getName());
        };
        Collections.sort(copy, order);
        return new ArrayList<>(copy.subList(0, Math.min(n, copy.size())));
    }

    // --- Допоміжні методи ---

    private static boolean near(double a, double b) {
        return Math.abs(a - b) < 1e-9;
    }

    private static void check(String name, boolean condition) {
        if (!condition) {
            failures++;
        }
        System.out.println((condition ? "OK   " : "FAIL ") + name);
    }

    private static void rejects(String name, Runnable action) {
        boolean thrown = false;
        try {
            action.run();
        } catch (IllegalArgumentException | UnsupportedOperationException expected) {
            thrown = true;
        }
        check("відхиляє: " + name, thrown);
    }
}
