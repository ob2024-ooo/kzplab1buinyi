import java.util.Objects;

/** Запис реєстру студентів для збереження у CSV (варіант 6). */
public final class LearnerRecord {
    @CsvColumn("ПІБ")
    private final String fullName;
    @CsvColumn("група")
    private final String group;
    @CsvColumn("курс")
    private final int studyYear;
    @CsvColumn("середній бал")
    private final double average;

    /**
     * Створює запис і перевіряє значення.
     *
     * @param fullName  ПІБ, не порожнє
     * @param group     група, не порожня
     * @param studyYear курс від 1 до 6
     * @param average   середній бал від 0 до 100
     * @throws IllegalArgumentException якщо значення неприпустиме
     */
    public LearnerRecord(String fullName, String group, int studyYear, double average) {
        if (fullName == null || fullName.isBlank() || group == null || group.isBlank()) {
            throw new IllegalArgumentException("порожнє ім'я або група");
        }
        if (studyYear < 1 || studyYear > 6) {
            throw new IllegalArgumentException("курс має бути від 1 до 6");
        }
        if (Double.isNaN(average) || average < 0 || average > 100) {
            throw new IllegalArgumentException("середній бал має бути від 0 до 100");
        }
        this.fullName = fullName;
        this.group = group;
        this.studyYear = studyYear;
        this.average = average;
    }

    /**
     * Створює запис зі студента з попередніх лабораторних.
     *
     * @param learner студент
     * @return запис для експорту
     */
    public static LearnerRecord from(Learner learner) {
        return new LearnerRecord(learner.getName(), learner.getGroup(),
                learner.getCourse(), learner.getAverage());
    }

    /** @return ПІБ */
    public String getFullName() {
        return fullName;
    }

    /** @return група */
    public String getGroup() {
        return group;
    }

    /** @return курс */
    public int getStudyYear() {
        return studyYear;
    }

    /** @return середній бал */
    public double getAverage() {
        return average;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LearnerRecord other)) {
            return false;
        }
        return studyYear == other.studyYear
                && Double.compare(average, other.average) == 0
                && fullName.equals(other.fullName)
                && group.equals(other.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, group, studyYear, average);
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.ROOT, "%s (%s, курс %d): %.2f",
                fullName, group, studyYear, average);
    }
}
