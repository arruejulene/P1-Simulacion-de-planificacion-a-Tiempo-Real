package proyecto1so.datastructures;

public class Queue<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    public Queue() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public void enqueue(T value) {
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

    public T dequeue() {
        if (head == null) return null;
        T v = head.value;
        head = head.next;
        if (head == null) tail = null;
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

