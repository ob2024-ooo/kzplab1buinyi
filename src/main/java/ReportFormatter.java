import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    /**
     * Формує розділ «Запити» з результатами п'яти запитів і пошуку.
     * Метод лише форматує результати {@link LearnerQueries}, а не обчислює їх.
     *
     * @param learners коректні студенти
     * @param topN кількість студентів у рейтингу
     * @param searchName ПІБ для пошуку або {@code null}, якщо пошук не потрібен
     * @return текст розділу
     */
    public static String formatQueries(List<Learner> learners, int topN, String searchName) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "Запити:%n"));
        List<Learner> excellent = LearnerQueries.excellent(learners);
        sb.append(String.format(Locale.ROOT, "Бал не нижче %.0f: %s%n",
                LearnerQueries.EXCELLENT_THRESHOLD, names(excellent)));
        sb.append(String.format(Locale.ROOT, "ПІБ усіх студентів: %s%n",
                String.join(", ", LearnerQueries.names(learners))));
        Map<String, Long> byGroup = LearnerQueries.countByGroup(learners);
        sb.append(String.format(Locale.ROOT, "Кількість за групами: %s%n",
                byGroup.entrySet().stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining(", "))));
        DoubleSummaryStatistics stats = LearnerQueries.scoreStatistics(learners);
        if (stats.getCount() == 0) {
            sb.append(String.format(Locale.ROOT, "Статистика балів: немає даних%n"));
        } else {
            sb.append(String.format(Locale.ROOT,
                    "Статистика балів: кількість=%d, мінімум=%.2f, середнє=%.2f, максимум=%.2f%n",
                    stats.getCount(), stats.getMin(), stats.getAverage(), stats.getMax()));
        }
        sb.append(String.format(Locale.ROOT, "Перші %d за балом:%n", topN));
        List<Learner> top = LearnerQueries.top(learners, topN);
        for (int i = 0; i < top.size(); i++) {
            sb.append(String.format(Locale.ROOT, "%d. %s (%.2f)%n",
                    i + 1, top.get(i).getName(), top.get(i).getAverage()));
        }
        if (searchName != null) {
            Optional<Learner> found = LearnerQueries.findByName(learners, searchName);
            sb.append(String.format(Locale.ROOT, "Пошук \"%s\": %s%n", searchName,
                    found.map(Learner::toString).orElse("не знайдено")));
        }
        return sb.toString();
    }

    /* Перетворює список студентів на рядок ПІБ через кому. */
    private static String names(List<Learner> learners) {
        return learners.isEmpty() ? "немає" : String.join(", ", LearnerQueries.names(learners));
    }
}
