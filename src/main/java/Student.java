/**
 * Модель одного запису реєстру студентів.
 *
 * @param name ПІБ студента
 * @param group назва групи
 * @param course курс (1-6)
 * @param average середній бал (0-100)
 * @param scholarship чи отримує стипендію
 */
public record Student(String name, String group, int course,
                      double average, boolean scholarship) {
}