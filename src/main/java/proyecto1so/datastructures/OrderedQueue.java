package proyecto1so.datastructures;

public class OrderedQueue<T> {
    private Node<T> head;
    private int size;
    private final Compare<T> comparator;

    public OrderedQueue(Compare<T> comparator) {
        this.head = null;
        this.size = 0;
        this.comparator = comparator;
    }

    public void insertOrdered(T value) {
        Node<T> n = new Node<>(value);

        if (head == null || comparator.compare(value, head.value) <= 0) {
            n.next = head;
            head = n;
            size++;
            return;
        }

        Node<T> cur = head;
        while (cur.next != null && comparator.compare(value, cur.next.value) > 0) {
            cur = cur.next;
        }

        n.next = cur.next;
        cur.next = n;
        size++;
    }

    public T dequeue() {
        if (head == null) return null;
        T v = head.value;
        head = head.next;
        size--;
        return v;
    }

    public T peek() {
        return head == null ? null : head.value;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
}
