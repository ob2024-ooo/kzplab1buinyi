/** Студент-бакалавр (курси 1-4): рейтинг дорівнює балу плюс бонус статусу. */
public final class Bachelor extends Learner {
    /** Найменший курс бакалавра. */
    public static final int MIN_COURSE = 1;
    /** Найбільший курс бакалавра. */
    public static final int MAX_COURSE = 4;

    /**
     * Створює бакалавра.
     *
     * @param name ПІБ
     * @param group група
     * @param course курс від 1 до 4
     * @param average середній бал від 0 до 100
     * @param status академічний статус
     * @throws IllegalArgumentException якщо порушено спільні інваріанти або курс не від 1 до 4
     */
    public Bachelor(String name, String group, int course,
                    double average, StudyStatus status) {
        super(name, group, course, average, status);
        if (course < MIN_COURSE || course > MAX_COURSE) {
            throw new IllegalArgumentException("бакалавр: курс має бути від 1 до 4");
        }
    }

    /** Рейтинг бакалавра: середній бал плюс бонус статусу. */
    @Override
    public double rating() {
        return getAverage() + getStatus().bonus();
    }

    /** Повертає {@code "бакалавр"}. */
    @Override
    public String degree() {
        return "бакалавр";
    }
}
