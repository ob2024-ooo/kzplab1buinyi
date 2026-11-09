import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Точка входу: консольний звіт про реєстр студентів. */
public final class Main {

    /* Забороняє створення екземплярів службового класу. */
    private Main() {
    }

    /**
     * Запускає програму.
     *
     * @param args {@code --help}, {@code --input файл}, {@code --output файл}
     */
    public static void main(String[] args) {
        // Явне UTF-8 для консолі: типове кодування Windows може не підтримувати українські літери.
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out),
                true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err),
                true, StandardCharsets.UTF_8));
        // Path сам обирає правильний роздільник для Windows, macOS та Ubuntu.
        Path input = Path.of("data", "input.csv");
        Path output = Path.of("out", "report.txt");
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--help".equals(arg)) {
                System.out.printf("Використання: java -cp out/classes Main "
                        + "[--help] [--input <файл>] [--output <файл>]%n");
                return;
            } else if ("--input".equals(arg) && i + 1 < args.length) {
                input = Path.of(args[++i]);
            } else if ("--output".equals(arg) && i + 1 < args.length) {
                output = Path.of(args[++i]);
            } else {
                System.err.printf("Невідомий або неповний аргумент: %s%n", arg);
                return;
            }
        }
        try {
            // Кодування задаємо явно, щоб українські літери читались однаково на всіх ОС.
            List<String> lines = Files.readAllLines(input, StandardCharsets.UTF_8);
            // Рядки перетворюються на об'єкти до обчислень: далі працюємо лише з Student.
            StudentParser.ParseResult result = StudentParser.parse(lines);
            GradeScholarship summary = StudentMetrics.summarize(result.students());
            String report = ReportFormatter.format(summary, result.errors());
            System.out.print(report);
            Path parent = output.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(output, report, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.printf("Помилка файла: %s%n", e.getMessage());
        }
    }
}
