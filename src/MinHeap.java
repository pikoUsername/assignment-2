import java.util.NoSuchElementException;

/**
 * Array-based binary min-heap of ints.
 *
 * Node i has children 2i+1 and 2i+2 and parent (i-1)/2.
 * Heap property: heap[parent(i)] <= heap[i] for every 0 < i < size.
 */
public class MinHeap {

    private static final int DEFAULT_CAPACITY = 16;

    private int[] heap;
    private int size;

    private long comparisons;
    private long swaps;

    public MinHeap() {
        this(DEFAULT_CAPACITY);
    }

    public MinHeap(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("capacity must be positive: " + initialCapacity);
        }
        heap = new int[initialCapacity];
    }

    /** Adds x as the last leaf and sifts it up. O(log n). */
    public void insert(int x) {
        if (size == heap.length) {
            int[] bigger = new int[heap.length * 2];
            System.arraycopy(heap, 0, bigger, 0, size);
            heap = bigger;
        }
        heap[size] = x;
        size++;
        siftUp(size - 1);
    }

    /** Returns the minimum without removing it. Theta(1). */
    public int peekMin() {
        if (size == 0) {
            throw new NoSuchElementException("heap is empty");
        }
        return heap[0];
    }

    /** Removes and returns the minimum: move the last leaf to the root and sift it down. O(log n). */
    public int extractMin() {
        if (size == 0) {
            throw new NoSuchElementException("heap is empty");
        }
        int min = heap[0];
        size--;
        if (size > 0) {
            heap[0] = heap[size];
            siftDown(0);
        }
        return min;
    }

    /**
     * Loop invariant (see README, proof 2): the array heap[0 .. size-1] satisfies the
     * heap property everywhere except possibly between i and parent(i).
     */
    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            comparisons++;
            if (heap[parent] <= heap[i]) {
                break;
            }
            swap(i, parent);
            i = parent;
        }
    }

    /**
     * Loop invariant: the heap property holds everywhere except possibly between
     * i and its children.
     */
    private void siftDown(int i) {
        while (true) {
            int left = 2 * i + 1;
            if (left >= size) {
                break;
            }
            int smallest = left;
            int right = left + 1;
            if (right < size) {
                comparisons++;
                if (heap[right] < heap[left]) {
                    smallest = right;
                }
            }
            comparisons++;
            if (heap[i] <= heap[smallest]) {
                break;
            }
            swap(i, smallest);
            i = smallest;
        }
    }

    private void swap(int a, int b) {
        int tmp = heap[a];
        heap[a] = heap[b];
        heap[b] = tmp;
        swaps++;
    }

    /** Checks the heap property for all nodes. Used by the tests. */
    public boolean isValidHeap() {
        for (int i = 1; i < size; i++) {
            if (heap[(i - 1) / 2] > heap[i]) {
                return false;
            }
        }
        return true;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getSwaps() {
        return swaps;
    }

    public void resetCounters() {
        comparisons = 0;
        swaps = 0;
    }
}
