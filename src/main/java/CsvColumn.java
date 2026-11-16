import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Позначає поле класу як стовпець CSV. Назва стовпця беруться лише з {@link #value()}.
 * Анотація доступна під час виконання (RUNTIME) і ставиться тільки на поля (FIELD).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvColumn {
    /** Назва стовпця в заголовку CSV. */
    String value();
}
