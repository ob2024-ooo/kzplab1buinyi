import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Імпорт записів {@link LearnerRecord} із CSV, створеного експортером. */
public final class LearnerCsvImporter {
    /* Службовий клас. */
    private LearnerCsvImporter() {
    }

    /**
     * Читає файл повністю в UTF-8 і відновлює записи.
     *
     * @param path файл CSV
     * @return відновлені записи (порожній список, якщо в файлі лише заголовок)
     * @throws CsvExportException помилка файла, формату, числа або значення
     */
    public static List<LearnerRecord> read(Path path) throws CsvExportException {
        String text;
        try {
            text = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CsvExportException("Не вдалося прочитати файл " + path, e);
        }
        return parse(text);
    }

    /**
     * Відновлює записи з тексту CSV.
     *
     * @param text текст із заголовком
     * @return відновлені записи
     * @throws CsvExportException помилка формату, числа або значення (з номером рядка)
     */
    public static List<LearnerRecord> parse(String text) throws CsvExportException {
        List<List<String>> rows = CsvParser.parse(text);
        if (rows.isEmpty()) {
            throw new CsvExportException("Порожній CSV: немає заголовка");
        }
        CsvExporter<LearnerRecord> exporter = new CsvExporter<>(LearnerRecord.class);
        List<String> expected = exporter.columns();
        if (!rows.get(0).equals(expected)) {
            throw new CsvExportException("Заголовок має бути: " + String.join(",", expected)
                    + "; знайдено: " + String.join(",", rows.get(0)));
        }
        // Порядок стовпців береться з експортера, тому читач не дублює його вручну.
        int iName = expected.indexOf("ПІБ");
        int iGroup = expected.indexOf("група");
        int iYear = expected.indexOf("курс");
        int iAvg = expected.indexOf("середній бал");
        List<LearnerRecord> result = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.size() != expected.size()) {
                throw new CsvExportException("Запис " + i + ": очікується " + expected.size()
                        + " поля, знайдено " + row.size());
            }
            try {
                result.add(new LearnerRecord(row.get(iName), row.get(iGroup),
                        Integer.parseInt(row.get(iYear).trim()),
                        Double.parseDouble(row.get(iAvg).trim())));
            } catch (NumberFormatException e) {
                throw new CsvExportException("Запис " + i + ": хибний числовий формат", e);
            } catch (IllegalArgumentException e) {
                throw new CsvExportException("Запис " + i + ": " + e.getMessage(), e);
            }
        }
        return result;
    }
}
