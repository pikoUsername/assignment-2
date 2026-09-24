/**
 * Common interface for the two sequence structures (DynamicArray, LinkedList).
 * It lets the benchmark and the tests run the same code against both.
 *
 * Every implementation keeps three operation counters:
 *   accesses    - reads/writes of stored elements (array slots or list nodes visited)
 *   comparisons - element comparisons (value == x) done by contains()
 *   moves       - elements shifted/copied (array) or links rewritten (list)
 */
public interface IndexedList {

    void add(int x);

    void add(int index, int x);

    int remove(int index);

    int get(int index);

    boolean contains(int x);

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    long getAccesses();

    long getComparisons();

    long getMoves();

    void resetCounters();

    String name();
}
