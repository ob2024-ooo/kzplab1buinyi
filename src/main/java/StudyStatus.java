/**
 * Академічний статус студента: від нього залежить бонус до рейтингу.
 *
 * <p>Значення enum фіксовані, тому їх можна безпечно порівнювати через
 * {@code ==} і використовувати в {@code switch}. Кожна константа має підпис
 * для звіту й бонусні бали.
 */
public enum StudyStatus {
    /** Студент отримує стипендію. */
    SCHOLARSHIP("стипендіат", 5.0),
    /** Студент без стипендії. */
    REGULAR("без стипендії", 0.0);

    private final String label;
    private final double bonus;

    /* Створює константу з підписом і бонусом; викликається лише самим enum. */
    StudyStatus(String label, double bonus) {
        this.label = label;
        this.bonus = bonus;
    }

    /** Повертає підпис статусу для звіту. */
    public String label() {
        return label;
    }

    /** Повертає бонусні бали, які статус додає до рейтингу. */
    public double bonus() {
        return bonus;
    }

    /**
     * Перетворює прапорець стипендії з вхідного файла на статус.
     *
     * @param scholarship {@code true}, якщо студент отримує стипендію
     * @return {@link #SCHOLARSHIP} або {@link #REGULAR}
     */
    public static StudyStatus of(boolean scholarship) {
        return scholarship ? SCHOLARSHIP : REGULAR;
    }
}
