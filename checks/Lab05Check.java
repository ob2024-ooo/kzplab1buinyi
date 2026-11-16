import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Ручна перевірка лабораторної роботи № 5 без JUnit: сховище, анотація, заголовок,
 * екранування, round-trip та помилки. Код завершення 1 означає помилку.
 */
public final class Lab05Check {
    private static int failures;
    private static int total;

    enum Genre { NOVEL, POEM }

    /** Інший домен: доводить, що експортер не прив'язаний до студентів. */
    static final class Book {
        @CsvColumn("назва")
        private final String title;
        @CsvColumn("дата")
        private final LocalDate published;
        @CsvColumn("жанр")
        private final Genre genre;
        @CsvColumn("сторінки")
        private final int pages;
        private final String hidden = "не експортується";

        Book(String title, LocalDate published, Genre genre, int pages) {
            this.title = title;
            this.published = published;
            this.genre = genre;
            this.pages = pages;
        }
    }

    /** Клас без анотацій. */
    static final class Plain {
        private final int x = 1;
    }

    /* Службовий клас. */
    private Lab05Check() {
    }

    /** Запускає всі перевірки. */
    public static void main(String[] args) throws Exception {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out),
                true, StandardCharsets.UTF_8));
        repositoryChecks();
        annotationChecks();
        escapeChecks();
        roundTripChecks();
        errorChecks();
        System.out.printf("Перевірок: %d, помилок: %d%n", total, failures);
        if (failures > 0) {
            System.exit(1);
        }
    }

    private static void check(String name, boolean ok) {
        total++;
        if (!ok) {
            failures++;
        }
        System.out.printf("%s: %s%n", ok ? "OK" : "FAIL", name);
    }

    private static void repositoryChecks() {
        Repository<LearnerRecord> repo = new Repository<>();
        LearnerRecord a = new LearnerRecord("Іваненко Олена", "КІ-303", 3, 92.5);
        LearnerRecord b = new LearnerRecord("Петренко Андрій", "КІ-301", 2, 78.0);
        repo.add(a);
        repo.add(b);
        List<LearnerRecord> snapshot = repo.all();
        repo.add(new LearnerRecord("Бондар Петро", "КІ-302", 2, 75));
        check("знімок не змінюється після add", snapshot.size() == 2 && repo.size() == 3);
        boolean immutable = false;
        try {
            snapshot.add(a);
        } catch (UnsupportedOperationException e) {
            immutable = true;
        }
        check("знімок незмінний", immutable);
        check("find за предикатом", repo.find(r -> r.getStudyYear() == 2).size() == 2);
        check("find без збігів", repo.find(r -> r.getAverage() > 100).isEmpty());
        boolean npe = false;
        try {
            repo.add(null);
        } catch (NullPointerException e) {
            npe = true;
        }
        check("add(null) відхиляється", npe);
    }

    private static void annotationChecks() throws Exception {
        check("анотація доступна під час виконання",
                LearnerRecord.class.getDeclaredField("fullName").getAnnotation(CsvColumn.class) != null);
        CsvExporter<LearnerRecord> exporter = new CsvExporter<>(LearnerRecord.class);
        check("порядок стовпців за іменами полів",
                exporter.columns().equals(List.of("середній бал", "ПІБ", "група", "курс")));
        check("заголовок порожнього списку",
                exporter.toCsv(List.of()).equals("середній бал,ПІБ,група,курс\n"));
        CsvExporter<Book> books = new CsvExporter<>(Book.class);
        String csv = books.toCsv(List.of(new Book("Кобзар", LocalDate.of(1840, 1, 1), Genre.POEM, 100)));
        check("інший клас: LocalDate, enum, int; поле без анотації пропущено",
                csv.equals("жанр,сторінки,дата,назва\nPOEM,100,1840-01-01,Кобзар\n"));
        checkFails("клас без анотацій", () -> new CsvExporter<>(Plain.class), null);
    }

    private static void escapeChecks() throws Exception {
        check("значення без спецсимволів", CsvExporter.escape("abc").equals("abc"));
        check("кома", CsvExporter.escape("a,b").equals("\"a,b\""));
        check("лапки", CsvExporter.escape("a\"b").equals("\"a\"\"b\""));
        check("перенесення рядка", CsvExporter.escape("a\nb").equals("\"a\nb\""));
        check("наївний split помиляється, парсер ні",
                "\"a,b\",c".split(",").length == 3
                        && CsvParser.parse("\"a,b\",c\n").get(0).equals(List.of("a,b", "c")));
        check("CRLF як межа запису", CsvParser.parse("a,b\r\nc,d\r\n").size() == 2);
        check("порожнє поле зберігається", CsvParser.parse("a,,c\n").get(0).size() == 3);
    }

    private static void roundTripChecks() throws Exception {
        List<LearnerRecord> original = new ArrayList<>(List.of(
                new LearnerRecord("Іваненко Олена", "КІ-303", 3, 92.5),
                new LearnerRecord("Петренко Андрій", "КІ-301", 2, 78.0),
                new LearnerRecord("Коваль, Марія \"Мері\"", "КІ-302", 4, 88.4),
                new LearnerRecord("Подвійне\nім'я", "КІ-301", 1, 64.2),
                new LearnerRecord("Лисенко Ірина", "КІ-303", 5, 95.1),
                new LearnerRecord("Крайній Випадок", "КІм-61", 6, 0)));
        CsvExporter<LearnerRecord> exporter = new CsvExporter<>(LearnerRecord.class);
        Path file = Path.of("out", "check-roundtrip.csv");
        exporter.write(file, original);
        String text = Files.readString(file, StandardCharsets.UTF_8);
        long physicalLines = text.lines().count();
        check("фізичних рядків 8 (заголовок + 6 записів + 1 перенесення)", physicalLines == 8);
        List<LearnerRecord> restored = LearnerCsvImporter.read(file);
        check("відновлено 6 об'єктів", restored.size() == 6);
        check("round-trip: списки рівні через equals", original.equals(restored));
        exporter.write(file, List.of());
        check("порожній набір: лише заголовок, імпорт дає 0 записів",
                LearnerCsvImporter.read(file).isEmpty());
    }

    private static void errorChecks() {
        checkFails("порожній файл", () -> LearnerCsvImporter.parse(""), null);
        checkFails("хибний заголовок",
                () -> LearnerCsvImporter.parse("ПІБ,група,курс,середній бал\n"), null);
        checkFails("хибна кількість полів",
                () -> LearnerCsvImporter.parse("середній бал,ПІБ,група,курс\n92.5,Олена,КІ-303\n"), null);
        checkFails("хибне число: cause NumberFormatException",
                () -> LearnerCsvImporter.parse("середній бал,ПІБ,група,курс\nabc,Олена,КІ-303,3\n"),
                NumberFormatException.class);
        checkFails("хибний курс: cause IllegalArgumentException",
                () -> LearnerCsvImporter.parse("середній бал,ПІБ,група,курс\n90,Олена,КІ-303,9\n"),
                IllegalArgumentException.class);
        checkFails("не закрито лапки",
                () -> LearnerCsvImporter.parse("середній бал,ПІБ,група,курс\n90,\"Олена,КІ-303,3\n"), null);
        checkFails("файл не існує: cause IOException",
                () -> LearnerCsvImporter.read(Path.of("out", "немає-такого.csv")), java.io.IOException.class);
        checkFails("запис у каталог без доступу: cause IOException", () -> {
            Path dir = Path.of("out", "dir-instead-of-file");
            Files.createDirectories(dir);
            new CsvExporter<>(LearnerRecord.class).write(dir, List.of());
        }, java.io.IOException.class);
    }

    private interface Action {
        void run() throws Exception;
    }

    /* Перевіряє, що дія кидає CsvExportException, а cause має очікуваний тип (null — cause не потрібен). */
    private static void checkFails(String name, Action action, Class<? extends Throwable> cause) {
        boolean ok = false;
        try {
            action.run();
        } catch (CsvExportException e) {
            ok = cause == null || (e.getCause() != null && cause.isInstance(e.getCause()));
        } catch (Exception e) {
            ok = false;
        }
        check(name, ok);
    }
}
