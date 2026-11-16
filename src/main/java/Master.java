/** Студент-магістр (курси 5-6): бал враховується з коефіцієнтом 1.1 плюс бонус статусу. */
public final class Master extends Learner {
    /** Найменший курс магістра. */
    public static final int MIN_COURSE = 5;
    /** Найбільший курс магістра. */
    public static final int MAX_COURSE = 6;
    /** Коефіцієнт, на який множиться середній бал магістра. */
    private static final double SCORE_FACTOR = 1.1;

    /**
     * Створює магістра.
     *
     * @param name ПІБ
     * @param group група
     * @param course курс від 5 до 6
     * @param average середній бал від 0 до 100
     * @param status академічний статус
     * @throws IllegalArgumentException якщо порушено спільні інваріанти або курс не від 5 до 6
     */
    public Master(String name, String group, int course,
                  double average, StudyStatus status) {
        super(name, group, course, average, status);
        if (course < MIN_COURSE || course > MAX_COURSE) {
            throw new IllegalArgumentException("магістр: курс має бути від 5 до 6");
        }
    }

    /** Рейтинг магістра: бал з коефіцієнтом 1.1 плюс бонус статусу. */
    @Override
    public double rating() {
        return getAverage() * SCORE_FACTOR + getStatus().bonus();
    }

    /** Повертає {@code "магістр"}. */
    @Override
    public String degree() {
        return "магістр";
    }
}
