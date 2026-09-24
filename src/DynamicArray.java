/**
 * Resizable array of ints (similar to java.util.ArrayList, but written from scratch).
 *
 * Elements are stored contiguously in data[0 .. size-1].
 * When the backing array is full it is replaced by one of double capacity.
 */
public class DynamicArray implements IndexedList {

    private static final int DEFAULT_CAPACITY = 8;

    private int[] data;
    private int size;

    private long accesses;
    private long comparisons;
    private long moves;

    public DynamicArray() {
        this(DEFAULT_CAPACITY);
    }

    public DynamicArray(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("capacity must be positive: " + initialCapacity);
        }
        data = new int[initialCapacity];
    }

    /** Appends x to the end. Amortized Theta(1). */
    @Override
    public void add(int x) {
        ensureCapacity(size + 1);
        data[size] = x;
        accesses++;
        size++;
    }

    /**
     * Inserts x at position index, shifting data[index .. size-1] one slot to the right.
     * Valid indices: 0 .. size (index == size appends).
     */
    @Override
    public void add(int index, int x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        ensureCapacity(size + 1);
        // Loop invariant (see README, proof 1):
        //   data[i+1 .. size] holds the original data[i .. size-1] and
        //   data[0 .. index-1] is unchanged.
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
            moves++;
        }
        data[index] = x;
        accesses++;
        size++;
    }

    /** Removes and returns the element at index, shifting the tail one slot to the left. */
    @Override
    public int remove(int index) {
        checkIndex(index);
        int removed = data[index];
        accesses++;
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
            moves++;
        }
        size--;
        return removed;
    }

    /** Direct address computation: Theta(1). */
    @Override
    public int get(int index) {
        checkIndex(index);
        accesses++;
        return data[index];
    }

    /** Linear search from the front. */
    @Override
    public boolean contains(int x) {
        for (int i = 0; i < size; i++) {
            accesses++;
            comparisons++;
            if (data[i] == x) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    public int capacity() {
        return data.length;
    }

    private void ensureCapacity(int required) {
        if (required <= data.length) {
            return;
        }
        int newCapacity = Math.max(required, data.length * 2);
        int[] bigger = new int[newCapacity];
        for (int i = 0; i < size; i++) {
            bigger[i] = data[i];
            moves++;
        }
        data = bigger;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    @Override
    public long getAccesses() {
        return accesses;
    }

    @Override
    public long getComparisons() {
        return comparisons;
    }

    @Override
    public long getMoves() {
        return moves;
    }

    @Override
    public void resetCounters() {
        accesses = 0;
        comparisons = 0;
        moves = 0;
    }

    @Override
    public String name() {
        return "DynamicArray";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(", ");
            sb.append(data[i]);
        }
        return sb.append(']').toString();
    }
}
