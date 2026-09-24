/**
 * Singly linked list of ints with head and tail references.
 *
 * Nodes are separate heap objects connected by "next" references,
 * so reaching position i always requires walking i links from the head.
 */
public class LinkedList implements IndexedList {

    private static final class Node {
        int value;
        Node next;

        Node(int value, Node next) {
            this.value = value;
            this.next = next;
        }
    }

    private Node head;
    private Node tail;
    private int size;

    private long accesses;
    private long comparisons;
    private long moves;

    /** Appends x using the tail reference: Theta(1). */
    @Override
    public void add(int x) {
        Node node = new Node(x, null);
        if (head == null) {
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;
        moves++;
        size++;
    }

    /** Inserts x at position index (0 .. size). Needs to walk to node index-1. */
    @Override
    public void add(int index, int x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (index == size) {
            add(x);
            return;
        }
        if (index == 0) {
            head = new Node(x, head);
            moves++;
        } else {
            Node prev = nodeAt(index - 1);
            prev.next = new Node(x, prev.next);
            moves += 2;
        }
        size++;
    }

    /** Removes and returns the element at index by relinking its predecessor. */
    @Override
    public int remove(int index) {
        checkIndex(index);
        int removed;
        if (index == 0) {
            removed = head.value;
            accesses++;
            head = head.next;
            moves++;
            if (head == null) {
                tail = null;
            }
        } else {
            Node prev = nodeAt(index - 1);
            Node target = prev.next;
            accesses++;
            removed = target.value;
            prev.next = target.next;
            moves++;
            if (target == tail) {
                tail = prev;
            }
        }
        size--;
        return removed;
    }

    /** Walks index links from the head: Theta(index + 1). */
    @Override
    public int get(int index) {
        checkIndex(index);
        return nodeAt(index).value;
    }

    /** Linear search from the head. */
    @Override
    public boolean contains(int x) {
        for (Node cur = head; cur != null; cur = cur.next) {
            accesses++;
            comparisons++;
            if (cur.value == x) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    /** Returns the node at position index; visits index + 1 nodes. */
    private Node nodeAt(int index) {
        Node cur = head;
        accesses++;
        for (int k = 0; k < index; k++) {
            cur = cur.next;
            accesses++;
        }
        return cur;
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
        return "LinkedList";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (Node cur = head; cur != null; cur = cur.next) {
            if (cur != head) sb.append(", ");
            sb.append(cur.value);
        }
        return sb.append(']').toString();
    }
}
