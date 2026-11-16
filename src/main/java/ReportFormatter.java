import java.util.List;
import java.util.Locale;

/** Формує текст звіту, не обчислюючи показників. */
public final class ReportFormatter {

    /* Забороняє створення екземплярів службового класу. */
    private ReportFormatter() {
    }

    /**
     * Формує звіт. Формат і назви показників збігаються з лабораторною
     * роботою № 1. {@code Locale.ROOT} дає крапку як десятковий роздільник,
     * {@code %n} дає роздільник рядків поточної ОС.
     *
     * @param summary підсумкові показники
     * @param errors повідомлення про пропущені рядки
     * @return готовий текст звіту
     */
    public static String format(GradeScholarship summary, List<String> errors) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "Лабораторна робота №1, варіант 6%n"));
        sb.append(String.format(Locale.ROOT, "Коректних записів: %d%n", summary.count()));
        sb.append(String.format(Locale.ROOT, "Середній бал: %.2f%n", summary.averageScore()));
        sb.append(String.format(Locale.ROOT, "Найбільший бал: %.2f%n", summary.maxScore()));
        sb.append(String.format(Locale.ROOT, "Кількість стипендіатів: %d%n",
                summary.scholarshipCount()));
        sb.append(String.format(Locale.ROOT, "Пропущено рядків: %d%n", errors.size()));
        for (String error : errors) {
            sb.append(String.format(Locale.ROOT, "%s%n", error));
        }
        return sb.toString();
    }

    /**
     * Формує додатковий розділ із рейтингами. Усі студенти обробляються через
     * спільний тип {@link Learner}: метод {@code rating()} сам обирає
     * реалізацію підтипу, тому тут немає {@code if} чи {@code switch} за типом.
     *
     * @param learners коректні студенти (бакалаври й магістри разом)
     * @return текст розділу «Рейтинги»
     */
    public static String formatRatings(List<Learner> learners) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "Рейтинги:%n"));
        for (Learner learner : learners) {
            sb.append(String.format(Locale.ROOT, "%s (%s, %s): %.2f%n",
                    learner.getName(), learner.degree(),
                    learner.getStatus().label(), learner.rating()));
        }
        return sb.toString();
    }
}
