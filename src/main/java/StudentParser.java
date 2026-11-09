import java.util.ArrayList;
import java.util.List;

/** Перетворює рядки вхідного файла на об'єкти {@link Student}. */
public final class StudentParser {

    /**
     * Результат розбору.
     *
     * @param students коректні студенти
     * @param errors повідомлення про пропущені рядки (з номером і причиною)
     */
    public record ParseResult(List<Student> students, List<String> errors) {
        /** Робить незмінні копії списків, щоб record був справді незмінним. */
        public ParseResult {
            students = List.copyOf(students);
            errors = List.copyOf(errors);
        }
    }

    /* Забороняє створення екземплярів службового класу. */
    private StudentParser() {
    }

    /**
     * Розбирає рядки. Хибний рядок не зупиняє програму й не потрапляє до
     * списку: його номер і причина додаються до списку помилок.
     *
     * @param lines рядки файла
     * @return коректні студенти та помилки
     */
    public static ParseResult parse(List<String> lines) {
        List<Student> students = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
            String line = lines.get(lineNumber - 1);
            try {
                // Об'єкт додається до списку лише після успішної перевірки.
                students.add(Student.fromCsv(line));
            } catch (IllegalArgumentException exception) {
                // Номер рядка відомий лише циклу читання, а не фабриці.
                errors.add("Рядок %d: %s".formatted(lineNumber, exception.getMessage()));
            }
        }
        return new ParseResult(students, errors);
    }
}
