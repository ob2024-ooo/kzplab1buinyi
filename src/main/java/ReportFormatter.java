import java.util.List;
import java.util.Locale;

/** Формує текст звіту, не обчислюючи показників. */
public final class ReportFormatter {

    /* Забороняє створення екземплярів службового класу. */
    private ReportFormatter() {
    }

    /**
     * Формує звіт. Locale.ROOT дає крапку як десятковий роздільник,
     * %n дає роздільник рядків поточної ОС.
     *
     * @param valid кількість коректних записів
     * @param average середній бал
     * @param max найбільший бал
     * @param scholarship кількість стипендіатів
     * @param errors повідомлення про пропущені рядки
     * @return готовий текст звіту
     */
    public static String format(int valid, double average, double max,
                                int scholarship, List<String> errors) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "Лабораторна робота №1, варіант 6%n"));
        sb.append(String.format(Locale.ROOT, "Коректних записів: %d%n", valid));
        sb.append(String.format(Locale.ROOT, "Середній бал: %.2f%n", average));
        sb.append(String.format(Locale.ROOT, "Найбільший бал: %.2f%n", max));
        sb.append(String.format(Locale.ROOT, "Кількість стипендіатів: %d%n", scholarship));
        sb.append(String.format(Locale.ROOT, "Пропущено рядків: %d%n", errors.size()));
        for (String error : errors) {
            sb.append(String.format(Locale.ROOT, "%s%n", error));
        }
        return sb.toString();
    }
}