/**
 * Перевірюваний виняток експорту та імпорту CSV. Зберігає причину ({@code cause}),
 * щоб початкову помилку (файл, число, рефлексія) не було втрачено.
 */
public class CsvExportException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Створює виняток без причини.
     *
     * @param message опис помилки
     */
    public CsvExportException(String message) {
        super(message);
    }

    /**
     * Створює виняток із початковою причиною.
     *
     * @param message опис помилки
     * @param cause   початкова помилка
     */
    public CsvExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
