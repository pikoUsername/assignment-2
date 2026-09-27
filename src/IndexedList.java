public interface IndexedList {
    void add(int x);
    void add(int index, int x);
    int remove(int index);
    int get(int index);
    boolean contains(int x);
    int size();

    long getAccesses();
    long getComparisons();
    long getMoves();
    void resetCounters();
    String name();
}
