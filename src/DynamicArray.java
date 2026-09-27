public class DynamicArray implements IndexedList {
    private int[] data = new int[8];
    private int size;
    private long accesses, comparisons, moves;

    public void add(int x) {
        add(size, x);
    }

    public void add(int index, int x) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException("Index: " + index);
        if (size == data.length) grow();
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
            moves++;
        }
        data[index] = x;
        accesses++;
        size++;
    }

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

    public int get(int index) {
        checkIndex(index);
        accesses++;
        return data[index];
    }

    public boolean contains(int x) {
        for (int i = 0; i < size; i++) {
            accesses++;
            comparisons++;
            if (data[i] == x) return true;
        }
        return false;
    }

    private void grow() {
        int[] bigger = new int[data.length * 2];
        for (int i = 0; i < size; i++) {
            bigger[i] = data[i];
            moves++;
        }
        data = bigger;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("Index: " + index);
    }

    public int size() { return size; }
    public long getAccesses() { return accesses; }
    public long getComparisons() { return comparisons; }
    public long getMoves() { return moves; }
    public void resetCounters() { accesses = comparisons = moves = 0; }
    public String name() { return "DynamicArray"; }
}
