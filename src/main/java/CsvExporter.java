import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Універсальний експортер у CSV. Не знає назв доменних класів: поля беруться
 * рефлексією за анотацією {@link CsvColumn}. Стовпці йдуть у порядку
 * сортування Java-імен полів, рядки розділяє символ «\n».
 *
 * @param <T> тип елементів, що експортуються
 */
public final class CsvExporter<T> {
    private static final String NEW_LINE = "\n";

    private final List<Field> fields;

    /**
     * Знаходить анотовані поля класу.
     *
     * @param type клас елементів (потрібен також для порожнього списку)
     * @throws CsvExportException якщо немає анотованих полів, назви повторюються
     *                            або доступ до поля заборонено
     */
    public CsvExporter(Class<T> type) throws CsvExportException {
        List<Field> found = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (Field field : type.getDeclaredFields()) {
            CsvColumn column = field.getAnnotation(CsvColumn.class);
            if (column == null || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (!names.add(column.value())) {
                throw new CsvExportException("Повторна назва стовпця: " + column.value());
            }
            if (!field.trySetAccessible()) {
                throw new CsvExportException("Немає доступу до поля: " + field.getName());
            }
            found.add(field);
        }
        if (found.isEmpty()) {
            throw new CsvExportException("У класі " + type.getSimpleName()
                    + " немає полів з анотацією @CsvColumn");
        }
        // getDeclaredFields() не гарантує порядок, тому сортуємо за Java-іменем.
        found.sort(Comparator.comparing(Field::getName));
        this.fields = List.copyOf(found);
    }

    /**
     * Повертає назви стовпців у порядку експорту.
     *
     * @return список назв з анотацій
     */
    public List<String> columns() {
        List<String> result = new ArrayList<>();
        for (Field field : fields) {
            result.add(field.getAnnotation(CsvColumn.class).value());
        }
        return result;
    }

    /**
     * Будує текст CSV: заголовок і по одному рядку на елемент.
     *
     * @param items елементи (може бути порожній список)
     * @return текст CSV
     * @throws CsvExportException якщо значення поля недоступне
     */
    public String toCsv(List<? extends T> items) throws CsvExportException {
        StringBuilder sb = new StringBuilder();
        sb.append(join(columns())).append(NEW_LINE);
        for (T item : items) {
            List<String> values = new ArrayList<>();
            for (Field field : fields) {
                try {
                    Object value = field.get(item);
                    values.add(value == null ? "" : String.valueOf(value));
                } catch (IllegalAccessException e) {
                    throw new CsvExportException("Не вдалося прочитати поле " + field.getName(), e);
                }
            }
            sb.append(join(values)).append(NEW_LINE);
        }
        return sb.toString();
    }

    /**
     * Записує CSV у файл у кодуванні UTF-8.
     *
     * @param path  файл призначення
     * @param items елементи
     * @throws CsvExportException якщо запис не вдався (cause — {@link IOException})
     */
    public void write(Path path, List<? extends T> items) throws CsvExportException {
        String text = toCsv(items);
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, text, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CsvExportException("Не вдалося записати файл " + path, e);
        }
    }

    /**
     * Екранує значення за правилами CSV: якщо є кома, лапка або перенесення рядка,
     * значення береться в лапки, а лапки всередині подвоюються.
     *
     * @param value значення
     * @return значення, придатне для CSV
     */
    static String escape(String value) {
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    private static String join(List<String> values) {
        List<String> escaped = new ArrayList<>();
        for (String value : values) {
            escaped.add(escape(value));
        }
        return String.join(",", escaped);
    }
}
