package proyecto1so.datastructures;

public class SingleLinkedList<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    public SingleLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public void addLast(T value) {
        Node<T> n = new Node<>(value);
        if (tail == null) {
            head = n;
            tail = n;
        } else {
            tail.next = n;
            tail = n;
        }
        size++;
    }

    public T removeFirst() {
        if (head == null) return null;
        T v = head.value;
        head = head.next;
        if (head == null) tail = null;
        size--;
        return v;
    }

    public T peekFirst() {
        return head == null ? null : head.value;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public interface Visitor<E> {
        void visit(E value);
    }

    public void forEach(Visitor<T> visitor) {
        Node<T> cur = head;
        while (cur != null) {
            visitor.visit(cur.value);
            cur = cur.next;
        }
    }
}
