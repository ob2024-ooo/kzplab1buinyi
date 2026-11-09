/**
 * Незмінний набір підсумкових показників реєстру студентів.
 *
 * <p>Це допоміжне значення замінює окремі змінні з лабораторної роботи № 1:
 * кількість коректних записів, середній бал, найбільший бал і кількість
 * стипендіатів. Компоненти не можна перепризначити після створення.
 *
 * @param count кількість коректних записів
 * @param averageScore середній бал (0, якщо записів немає)
 * @param maxScore найбільший бал (0, якщо записів немає)
 * @param scholarshipCount кількість стипендіатів
 */
public record GradeScholarship(int count, double averageScore,
                               double maxScore, int scholarshipCount) {
    /**
     * Перевіряє допустимість підсумкових показників.
     *
     * @throws IllegalArgumentException якщо кількість від'ємна, стипендіатів
     *         більше, ніж записів, або бали поза діапазоном 0-100
     */
    public GradeScholarship {
        if (count < 0 || scholarshipCount < 0 || scholarshipCount > count) {
            throw new IllegalArgumentException("Некоректна кількість записів");
        }
        // Перевірка !(x >= 0 && x <= 100) відсікає й NaN.
        if (!(averageScore >= 0 && averageScore <= 100)
                || !(maxScore >= 0 && maxScore <= 100)) {
            throw new IllegalArgumentException("Бали мають бути від 0 до 100");
        }
    }
}
