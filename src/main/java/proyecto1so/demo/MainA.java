package proyecto1so.demo;

import proyecto1so.datastructures.Queue;
import proyecto1so.datastructures.SingleLinkedList;

public class MainA {
    public static void main(String[] args) {
        checkQueue();
        System.out.println();
        checkSingleLinkedList();
    }

    private static void checkQueue() {
        System.out.println("=== checkQueue ===");
        Queue<Integer> q = new Queue<>();

        q.enqueue(10);
        q.enqueue(20);
        q.enqueue(30);

        System.out.println("peek=" + q.peek());      // 10
        System.out.println("deq=" + q.dequeue());    // 10
        System.out.println("deq=" + q.dequeue());    // 20
        System.out.println("deq=" + q.dequeue());    // 30
        System.out.println("deq=" + q.dequeue());    // null
        System.out.println("empty=" + q.isEmpty());  // true
        System.out.println("size=" + q.size());      // 0
    }

    private static void checkSingleLinkedList() {
        System.out.println("=== checkSingleLinkedList ===");
        SingleLinkedList<Integer> list = new SingleLinkedList<>();

        list.addLast(1);
        list.addLast(2);
        list.addLast(3);

        System.out.println("size=" + list.size());      // 3
        System.out.println("empty=" + list.isEmpty());  // false
        System.out.println("peek=" + list.peekFirst()); // 1

        System.out.print("forEach=");
        list.forEach(new SingleLinkedList.Visitor<Integer>() {
            @Override
            public void visit(Integer value) {
                System.out.print(value + " ");
            }
        });
        System.out.println();

        System.out.println("rm=" + list.removeFirst()); // 1
        System.out.println("rm=" + list.removeFirst()); // 2
        System.out.println("rm=" + list.removeFirst()); // 3
        System.out.println("rm=" + list.removeFirst()); // null

        System.out.println("size=" + list.size());      // 0
        System.out.println("empty=" + list.isEmpty());  // true
        System.out.println("peek=" + list.peekFirst()); // null
    }
}
