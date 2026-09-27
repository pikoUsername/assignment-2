public class LinkedList implements IndexedList {
    private static class Node {
        int value;
        Node next;
        Node(int value, Node next) { this.value = value; this.next = next; }
    }

    private Node head, tail;
    private int size;
    private long accesses, comparisons, moves;

    public void add(int x) {
        Node node = new Node(x, null);
        if (head == null) head = node;
        else tail.next = node;
        tail = node;
        moves++;
        size++;
    }

    public void add(int index, int x) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException("Index: " + index);
        if (index == size) { add(x); return; }
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

    public int remove(int index) {
        checkIndex(index);
        Node removed;
        if (index == 0) {
            removed = head;
            head = head.next;
            if (head == null) tail = null;
        } else {
            Node prev = nodeAt(index - 1);
            removed = prev.next;
            prev.next = removed.next;
            if (removed == tail) tail = prev;
        }
        accesses++;
        moves++;
        size--;
        return removed.value;
    }

    public int get(int index) {
        checkIndex(index);
        return nodeAt(index).value;
    }

    public boolean contains(int x) {
        for (Node cur = head; cur != null; cur = cur.next) {
            accesses++;
            comparisons++;
            if (cur.value == x) return true;
        }
        return false;
    }

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
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("Index: " + index);
    }

    public int size() { return size; }
    public long getAccesses() { return accesses; }
    public long getComparisons() { return comparisons; }
    public long getMoves() { return moves; }
    public void resetCounters() { accesses = comparisons = moves = 0; }
    public String name() { return "LinkedList"; }
}
