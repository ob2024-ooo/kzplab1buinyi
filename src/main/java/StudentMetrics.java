import java.util.List;

/** Обчислює показники за списком коректних студентів. */
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
    public static double averageScore(List<Student> students) {
        if (students.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (Student s : students) {
            sum += s.average();
        }
        // sum має тип double, тому дробова частина зберігається.
        return sum / students.size();
    }

    /**
     * Найбільший середній бал.
     *
     * @param students коректні записи
     * @return максимум, або 0.0 для порожнього списку
     */
    public static double maxScore(List<Student> students) {
        double max = 0.0;
        for (Student s : students) {
            max = Math.max(max, s.average());
        }
        return max;
    }

    /**
     * Кількість стипендіатів.
     *
     * @param students коректні записи
     * @return кількість записів зі scholarship = true
     */
    public static int countScholarship(List<Student> students) {
        int count = 0;
        for (Student s : students) {
            if (s.scholarship()) {
                count++;
            }
        }
        return count;
    }
}