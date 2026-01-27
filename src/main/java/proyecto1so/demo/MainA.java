package proyecto1so.demo;

import proyecto1so.datastructures.Compare;
import proyecto1so.datastructures.OrderedQueue;
import proyecto1so.datastructures.Queue;

public class MainA {
    public static void main(String[] args) {
        checkQueue();
        System.out.println();
        checkOrderedQueue();
    }

    private static void checkQueue() {
        System.out.println("=== checkQueue ===");
        Queue<Integer> q = new Queue<>();

        q.enqueue(10);
        q.enqueue(20);
        q.enqueue(30);

        System.out.println("peek=" + q.peek());
        System.out.println("deq=" + q.dequeue());
        System.out.println("deq=" + q.dequeue());
        System.out.println("deq=" + q.dequeue());
        System.out.println("deq=" + q.dequeue());
        System.out.println("empty=" + q.isEmpty());
        System.out.println("size=" + q.size());
    }

    private static void checkOrderedQueue() {
        System.out.println("=== checkOrderedQueue ===");

        Compare<Integer> asc = new Compare<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return a - b;
            }
        };

        OrderedQueue<Integer> oq = new OrderedQueue<>(asc);

        oq.insertOrdered(50);
        oq.insertOrdered(10);
        oq.insertOrdered(30);
        oq.insertOrdered(20);
        oq.insertOrdered(40);
        oq.insertOrdered(5);
        oq.insertOrdered(60);
        oq.insertOrdered(15);
        oq.insertOrdered(35);
        oq.insertOrdered(25);

        while (!oq.isEmpty()) {
            System.out.println("deq=" + oq.dequeue());
        }
    }
}
