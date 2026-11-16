import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Запити Stream API до колекції студентів варіанта 6.
 *
 * <p>Кожен запит є окремим методом з одним зрозумілим результатом. Методи
 * працюють зі спільним типом {@link Learner} (без {@code if} за підтипом),
 * не змінюють вхідну колекцію й не форматують вивід.
 */
public final class LearnerQueries {
    /** Нижня межа балу для відбору відмінників (включно). */
    public static final double EXCELLENT_THRESHOLD = 90.0;

    /* Забороняє створення екземплярів службового класу. */
    private LearnerQueries() {
    }

    /**
     * Запит 1 (filter): студенти з балом не нижче {@value #EXCELLENT_THRESHOLD}.
     *
     * @param learners вхідний список, не змінюється
     * @return нова незмінна колекція відмінників у порядку вхідного списку
     */
    public static List<Learner> excellent(List<Learner> learners) {
        return learners.stream()
                .filter(learner -> learner.getAverage() >= EXCELLENT_THRESHOLD)
                .toList();
    }

    /**
     * Запит 2 (map): ПІБ усіх студентів у порядку вхідного списку.
     *
     * @param learners вхідний список, не змінюється
     * @return незмінний список ПІБ (дублікати зберігаються)
     */
    public static List<String> names(List<Learner> learners) {
        return learners.stream()
                .map(Learner::getName)
                .toList();
    }

    /**
     * Запит 3 (groupingBy): кількість студентів за групами.
     * Ключ карти — назва групи, значення — кількість. {@link TreeMap} дає
     * стабільний порядок ключів.
     *
     * @param learners вхідний список, не змінюється
     * @return карта «група → кількість»; порожня для порожнього списку
     */
    public static Map<String, Long> countByGroup(List<Learner> learners) {
        return learners.stream()
                .collect(Collectors.groupingBy(Learner::getGroup, TreeMap::new,
                        Collectors.counting()));
    }

    /**
     * Запит 4 (summarizingDouble): статистика середніх балів.
     * Для порожнього списку кількість дорівнює 0, сума 0.0, а мінімум і
     * максимум дорівнюють {@code +Infinity} і {@code -Infinity}, тому
     * перед читанням цих значень слід перевіряти {@code getCount()}.
     *
     * @param learners вхідний список, не змінюється
     * @return кількість, сума, мінімум, середнє й максимум балів
     */
    public static DoubleSummaryStatistics scoreStatistics(List<Learner> learners) {
        return learners.stream()
                .collect(Collectors.summarizingDouble(Learner::getAverage));
    }

    /**
     * Запит 5 (sorted + limit): перші N студентів за балом спаданням, а за
     * однакового балу за ПІБ зростанням. {@code reversed()} застосовується
     * лише до першого ключа, до {@code thenComparing}.
     *
     * @param learners вхідний список, не змінюється
     * @param n кількість результатів, не від'ємна (0 дає порожній список,
     *          завелике N дає всіх студентів)
     * @return незмінний список перших N студентів
     * @throws IllegalArgumentException якщо {@code n} від'ємне
     */
    public static List<Learner> top(List<Learner> learners, int n) {
        if (n < 0) {
            throw new IllegalArgumentException("N не може бути від'ємним");
        }
        Comparator<Learner> byScoreThenName = Comparator
                .comparingDouble(Learner::getAverage).reversed()
                .thenComparing(Learner::getName);
        return learners.stream()
                .sorted(byScoreThenName)
                .limit(n)
                .toList();
    }

    /**
     * Пошук за ПІБ. Якщо ключ повторюється, повертається перший за порядком
     * у списку студент.
     *
     * @param learners вхідний список, не змінюється
     * @param name ПІБ для пошуку
     * @return знайдений студент або {@link Optional#empty()}
     */
    public static Optional<Learner> findByName(List<Learner> learners, String name) {
        if (name == null) {
            return Optional.empty();
        }
        String key = name.trim();
        return learners.stream()
                .filter(learner -> learner.getName().equals(key))
                .findFirst();
    }
}
