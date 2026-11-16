import java.util.DoubleSummaryStatistics;
import java.util.List;

/**
 * Обчислює показники за списком коректних студентів через Stream API.
 * Результати збігаються з циклами лабораторних робіт № 1-3.
 */
public final class StudentMetrics {

    /* Забороняє створення екземплярів службового класу. */
    private StudentMetrics() {
    }

    /**
     * Середній бал.
     *
     * @param students коректні записи
     * @return середнє значення, або 0.0 для порожнього списку
     */
    public static double averageScore(List<Learner> students) {
        DoubleSummaryStatistics statistics = LearnerQueries.scoreStatistics(students);
        // Для порожнього потоку getAverage() уже дає 0.0, але перевірка робить це явним.
        return statistics.getCount() == 0 ? 0.0 : statistics.getAverage();
    }

    /**
     * Найбільший середній бал.
     *
     * @param students коректні записи
     * @return максимум, або 0.0 для порожнього списку
     */
    public static double maxScore(List<Learner> students) {
        DoubleSummaryStatistics statistics = LearnerQueries.scoreStatistics(students);
        // Для порожнього потоку getMax() дорівнює -Infinity, тому потрібна перевірка.
        return statistics.getCount() == 0 ? 0.0 : statistics.getMax();
    }

    /**
     * Кількість стипендіатів.
     *
     * @param students коректні записи
     * @return кількість студентів зі стипендією
     */
    public static int countScholarship(List<Learner> students) {
        return (int) students.stream().filter(Learner::isScholarship).count();
    }

    /**
     * Збирає всі чотири показники варіанта 6 в один незмінний record.
     *
     * @param students коректні записи
     * @return підсумкові показники
     */
    public static GradeScholarship summarize(List<Learner> students) {
        return new GradeScholarship(
                students.size(),
                averageScore(students),
                maxScore(students),
                countScholarship(students));
    }
}
