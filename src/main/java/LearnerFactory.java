import java.util.Objects;

/**
 * Створює об'єкти {@link Learner} з рядків CSV.
 *
 * <p>Формат вхідного файла не змінився:
 * {@code name;group;course;average;scholarship}. Підтип визначається курсом:
 * курси 1-4 дають {@link Bachelor}, 5-6 дають {@link Master}. Цей вибір
 * робиться в одному місці й не поширюється на код звітності.
 */
public final class LearnerFactory {

    /* Забороняє створення екземплярів службового класу. */
    private LearnerFactory() {
    }

    /**
     * Створює студента з рядка CSV.
     *
     * @param line рядок CSV, не {@code null}
     * @return {@link Bachelor} або {@link Master}
     * @throws IllegalArgumentException якщо рядок порожній, має неправильну
     *         кількість полів, помилковий числовий формат або порушує інваріант
     */
    public static Learner fromCsv(String line) {
        Objects.requireNonNull(line, "Рядок не може бути null");
        if (line.isBlank()) {
            throw new IllegalArgumentException("порожній рядок");
        }
        // -1 зберігає порожнє останнє поле, тому кількість полів перевіряється точно.
        String[] fields = line.split(";", -1);
        if (fields.length != 5) {
            throw new IllegalArgumentException(
                    "очікується 5 полів, знайдено %d".formatted(fields.length));
        }
        // Порядок перевірок такий самий, як у лабораторних роботах № 1 і № 2.
        Learner.requireText(fields[0], fields[1]);
        String flag = fields[4].trim();
        if (!"true".equalsIgnoreCase(flag) && !"false".equalsIgnoreCase(flag)) {
            throw new IllegalArgumentException(
                    "поле scholarship має бути true або false");
        }
        try {
            int course = Integer.parseInt(fields[2].trim());
            double average = Double.parseDouble(fields[3].trim());
            StudyStatus status = StudyStatus.of(Boolean.parseBoolean(flag));
            // Курс поза 1-6 потрапляє в Bachelor/Master і відхиляється спільним
            // правилом базового класу з тим самим повідомленням, що й раніше.
            if (course <= Bachelor.MAX_COURSE) {
                return new Bachelor(fields[0], fields[1], course, average, status);
            }
            return new Master(fields[0], fields[1], course, average, status);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "курс або бал має помилковий числовий формат", exception);
        }
    }
}
