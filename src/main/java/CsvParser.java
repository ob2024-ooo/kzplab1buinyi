import java.util.ArrayList;
import java.util.List;

/** Документний розбір CSV: лапки можуть містити коми, подвоєні лапки та перенесення рядка. */
public final class CsvParser {
    /* Службовий клас. */
    private CsvParser() {
    }

    /**
     * Розбирає весь текст на записи та поля. Закінчення рядка {@code \n} і {@code \r\n}
     * вважаються межею запису лише поза лапками.
     *
     * @param text текст CSV
     * @return список записів; порожній текст дає порожній список
     * @throws CsvExportException якщо лапки не закрито або після закритих лапок є зайві символи
     */
    public static List<List<String>> parse(String text) throws CsvExportException {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        boolean afterQuote = false;
        boolean pending = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                        afterQuote = true;
                    }
                } else {
                    field.append(c);
                }
            } else if (c == '"' && field.length() == 0 && !afterQuote) {
                inQuotes = true;
                pending = true;
            } else if (afterQuote && c != ',' && c != '\n' && c != '\r') {
                throw new CsvExportException("Символи після закритих лапок");
            } else if (c == ',') {
                row.add(field.toString());
                field.setLength(0);
                afterQuote = false;
                pending = true;
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
                row.add(field.toString());
                rows.add(row);
                row = new ArrayList<>();
                field.setLength(0);
                afterQuote = false;
                pending = false;
            } else {
                field.append(c);
                pending = true;
            }
        }
        if (inQuotes) {
            throw new CsvExportException("Не закрито лапки у CSV");
        }
        if (pending || field.length() > 0) {
            row.add(field.toString());
            rows.add(row);
        }
        return rows;
    }
}
