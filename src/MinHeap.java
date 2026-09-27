import java.util.Arrays;
import java.util.NoSuchElementException;

public class MinHeap {
    private int[] heap = new int[16];
    private int size;
    private long comparisons;

    public void insert(int x) {
        if (size == heap.length) heap = Arrays.copyOf(heap, size * 2);
        heap[size] = x;
        int i = size++;
        while (i > 0) {
            int parent = (i - 1) / 2;
            comparisons++;
            if (heap[parent] <= heap[i]) break;
            swap(i, parent);
            i = parent;
        }
    }

    public int peekMin() {
        if (size == 0) throw new NoSuchElementException("heap is empty");
        return heap[0];
    }

    public int extractMin() {
        int min = peekMin();
        heap[0] = heap[--size];
        int i = 0;
        while (2 * i + 1 < size) {
            int child = 2 * i + 1;
            if (child + 1 < size) {
                comparisons++;
                if (heap[child + 1] < heap[child]) child++;
            }
            comparisons++;
            if (heap[i] <= heap[child]) break;
            swap(i, child);
            i = child;
        }
        return min;
    }

    private void swap(int a, int b) {
        int t = heap[a];
        heap[a] = heap[b];
        heap[b] = t;
    }

    public boolean isValidHeap() {
        for (int i = 1; i < size; i++) if (heap[(i - 1) / 2] > heap[i]) return false;
        return true;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
    public long getComparisons() { return comparisons; }
}
