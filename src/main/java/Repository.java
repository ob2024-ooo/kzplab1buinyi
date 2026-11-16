import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Просте параметризоване сховище в пам'яті.
 *
 * @param <T> тип елементів сховища
 */
public final class Repository<T> {
    private final List<T> items = new ArrayList<>();

    /**
     * Додає елемент.
     *
     * @param item елемент, не {@code null}
     */
    public void add(T item) {
        items.add(Objects.requireNonNull(item, "елемент не може бути null"));
    }

    /**
     * Повертає незмінний знімок: пізніші зміни сховища на нього не впливають.
     *
     * @return копія списку елементів
     */
    public List<T> all() {
        return List.copyOf(items);
    }

    /**
     * Шукає елементи за умовою.
     *
     * @param predicate умова відбору
     * @return незмінний список відповідних елементів у порядку додавання
     */
    public List<T> find(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "умова не може бути null");
        List<T> result = new ArrayList<>();
        for (T item : items) {
            if (predicate.test(item)) {
                result.add(item);
            }
        }
        return List.copyOf(result);
    }

    /**
     * Повертає кількість елементів.
     *
     * @return кількість елементів
     */
    public int size() {
        return items.size();
    }
}
