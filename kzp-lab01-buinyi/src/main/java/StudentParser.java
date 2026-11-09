import java.util.ArrayList;
import java.util.List;

/** Розбирає та перевіряє рядки вхідного файла. */
public final class StudentParser {

    /**
     * Результат розбору.
     *
     * @param students коректні записи
     * @param errors повідомлення про пропущені рядки (з номером і причиною)
     */
    public record ParseResult(List<Student> students, List<String> errors) {
    }

    /* Забороняє створення екземплярів службового класу. */
    private StudentParser() {
    }

    /**
     * Розбирає рядки. Хибний рядок не зупиняє програму: його номер і причина
     * потрапляють у список помилок.
     *
     * @param lines рядки файла
     * @return коректні записи та помилки
     */
    public static ParseResult parse(List<String> lines) {
        List<Student> students = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            int number = i + 1;
            String line = lines.get(i);
            if (line.isBlank()) {
                errors.add("Рядок %d: порожній рядок".formatted(number));
                continue;
            }
            // -1 зберігає порожнє останнє поле.
            String[] f = line.split(";", -1);
            if (f.length != 5) {
                errors.add("Рядок %d: очікується 5 полів, знайдено %d"
                        .formatted(number, f.length));
                continue;
            }
            String name = f[0].trim();
            String group = f[1].trim();
            if (name.isEmpty() || group.isEmpty()) {
                errors.add("Рядок %d: порожнє ім'я або група".formatted(number));
                continue;
            }
            String flag = f[4].trim();
            if (!"true".equalsIgnoreCase(flag) && !"false".equalsIgnoreCase(flag)) {
                errors.add("Рядок %d: поле scholarship має бути true або false"
                        .formatted(number));
                continue;
            }
            try {
                int course = Integer.parseInt(f[2].trim());
                double average = Double.parseDouble(f[3].trim());
                if (course < 1 || course > 6) {
                    errors.add("Рядок %d: курс має бути від 1 до 6".formatted(number));
                    continue;
                }
                if (Double.isNaN(average) || average < 0 || average > 100) {
                    errors.add("Рядок %d: середній бал має бути від 0 до 100"
                            .formatted(number));
                    continue;
                }
                students.add(new Student(name, group, course, average,
                        Boolean.parseBoolean(flag)));
            } catch (NumberFormatException e) {
                errors.add("Рядок %d: курс або бал має помилковий числовий формат"
                        .formatted(number));
            }
        }
        return new ParseResult(students, errors);
    }
}