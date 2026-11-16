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
    /** Кількість студентів у рейтингу запиту «топ-N». */
    private static final int TOP_N = 5;

    /* Забороняє створення екземплярів службового класу. */
    private Main() {
    }

    /**
     * Запускає програму.
     *
     * @param args {@code --help}, {@code --input файл}, {@code --output файл},
     *             {@code --ratings} (додатково вивести рейтинги),
     *             {@code --queries} (додатково вивести запити Stream API),
     *             {@code --find ПІБ} (пошук за ПІБ у розділі запитів),
     *             {@code --export файл} (зберегти студентів у CSV і перевірити round-trip),
     *             {@code --import файл} (прочитати CSV, створений експортом)
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
        boolean ratings = false;
        boolean queries = false;
        String find = null;
        Path export = null;
        Path importPath = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--help".equals(arg)) {
                System.out.printf("Використання: java -cp out/classes Main "
                        + "[--help] [--input <файл>] [--output <файл>] [--ratings] [--queries] [--find <ПІБ>] [--export <файл>] [--import <файл>]%n");
                return;
            } else if ("--ratings".equals(arg)) {
                ratings = true;
            } else if ("--queries".equals(arg)) {
                queries = true;
            } else if ("--find".equals(arg) && i + 1 < args.length) {
                find = args[++i];
                queries = true;
            } else if ("--export".equals(arg) && i + 1 < args.length) {
                export = Path.of(args[++i]);
            } else if ("--import".equals(arg) && i + 1 < args.length) {
                importPath = Path.of(args[++i]);
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
            // Рядки перетворюються на об'єкти до обчислень: далі працюємо лише зі спільним типом Learner.
            StudentParser.ParseResult result = StudentParser.parse(lines);
            GradeScholarship summary = StudentMetrics.summarize(result.students());
            String report = ReportFormatter.format(summary, result.errors());
            if (ratings) {
                // Рейтинг обчислюється поліморфно: список містить і бакалаврів, і магістрів.
                report += ReportFormatter.formatRatings(result.students());
            }
            if (queries) {
                // Запити працюють зі спільним типом Learner і не змінюють список.
                report += ReportFormatter.formatQueries(result.students(), TOP_N, find);
            }
            System.out.print(report);
            Path parent = output.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(output, report, StandardCharsets.UTF_8);
            if (export != null) {
                exportAndCheck(result.students(), export);
            }
            if (importPath != null) {
                List<LearnerRecord> records = LearnerCsvImporter.read(importPath);
                System.out.printf("Імпортовано записів: %d%n", records.size());
                for (LearnerRecord record : records) {
                    System.out.println(record);
                }
            }
        } catch (IOException e) {
            System.err.printf("Помилка файла: %s%n", e.getMessage());
        } catch (CsvExportException e) {
            // Причину не ховаємо: показуємо й повідомлення, і cause.
            System.err.printf("Помилка CSV: %s%n", e.getMessage());
            if (e.getCause() != null) {
                System.err.printf("Причина: %s%n", e.getCause());
            }
        }
    }

    /* Зберігає студентів у CSV, читає назад і порівнює списки через equals. */
    private static void exportAndCheck(List<Learner> students, Path file) throws CsvExportException {
        Repository<LearnerRecord> repository = new Repository<>();
        for (Learner learner : students) {
            repository.add(LearnerRecord.from(learner));
        }
        List<LearnerRecord> original = repository.all();
        new CsvExporter<>(LearnerRecord.class).write(file, original);
        List<LearnerRecord> restored = LearnerCsvImporter.read(file);
        System.out.printf("Експортовано записів: %d (файл %s)%n", original.size(), file);
        System.out.printf("Round-trip: %s%n", original.equals(restored) ? "списки рівні" : "списки відрізняються");
    }
}
