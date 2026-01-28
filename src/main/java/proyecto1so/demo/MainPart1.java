package proyecto1so.demo;

import proyecto1so.datastructures.Queue;
import proyecto1so.datastructures.OrderedQueue;
import proyecto1so.datastructures.Compare;
import proyecto1so.datastructures.SingleLinkedList;

import proyecto1so.generator.ProcessGenerator;

import proyecto1so.memory.MemoryManager;
import proyecto1so.memory.SuspensionPolicy;

import proyecto1so.model.PCB;
import proyecto1so.model.ProcessState;
import proyecto1so.model.StateManager;
import proyecto1so.model.TaskType;

public class MainPart1 {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("DEMO MAIN (Persona A) - PART 1");
        System.out.println("========================================\n");

        testStructures();
        testPCBAndTransitions();
        testMemoryAndGenerator();
    }

    private static void testStructures() {
        System.out.println("=== 1) STRUCTURES TEST ===");

        Queue<Integer> q = new Queue<>();
        q.enqueue(10);
        q.enqueue(20);
        q.enqueue(30);
        System.out.println("[Queue] peek=" + q.peek() + " (expected 10)");
        System.out.println("[Queue] deq=" + q.dequeue() + " (expected 10)");
        System.out.println("[Queue] deq=" + q.dequeue() + " (expected 20)");
        System.out.println("[Queue] deq=" + q.dequeue() + " (expected 30)");
        System.out.println("[Queue] empty=" + q.isEmpty() + " (expected true)");
        System.out.println("[Queue] size=" + q.size() + " (expected 0)");

        SingleLinkedList<String> list = new SingleLinkedList<>();
        list.addLast("A");
        list.addLast("B");
        list.addLast("C");
        System.out.println("[SingleLinkedList] peekFirst=" + list.peekFirst() + " (expected A)");
        System.out.println("[SingleLinkedList] removeFirst=" + list.removeFirst() + " (expected A)");
        System.out.println("[SingleLinkedList] peekFirst=" + list.peekFirst() + " (expected B)");
        System.out.println("[SingleLinkedList] size=" + list.size() + " (expected 2)");

        Compare<Integer> asc = new Compare<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                if (a == null && b == null) return 0;
                if (a == null) return -1;
                if (b == null) return 1;
                if (a < b) return -1;
                if (a > b) return 1;
                return 0;
            }
        };

        OrderedQueue<Integer> oq = new OrderedQueue<>(asc);
        oq.insertOrdered(50);
        oq.insertOrdered(10);
        oq.insertOrdered(30);
        oq.insertOrdered(20);
        oq.insertOrdered(40);

        System.out.print("[OrderedQueue asc] deq order: ");
        while (!oq.isEmpty()) {
            System.out.print(oq.dequeue() + " ");
        }
        System.out.println("(expected 10 20 30 40 50)");

        System.out.println();
    }

    private static void testPCBAndTransitions() {
        System.out.println("=== 2) PCB + TRANSITIONS TEST ===");

        PCB p = new PCB(
                1,
                "P1",
                3,
                50,
                20,
                TaskType.CPU_BOUND,
                false,
                0,
                0,
                0
        );

        System.out.println("INITIAL: " + p.getName() + " state=" + p.getState());

        System.out.println("NEW -> READY: " + StateManager.transition(p, ProcessState.READY) + " state=" + p.getState());
        System.out.println("READY -> RUNNING: " + StateManager.transition(p, ProcessState.RUNNING) + " state=" + p.getState());
        System.out.println("RUNNING -> BLOCKED: " + StateManager.transition(p, ProcessState.BLOCKED) + " state=" + p.getState());
        System.out.println("BLOCKED -> READY: " + StateManager.transition(p, ProcessState.READY) + " state=" + p.getState());

        System.out.println("INVALID (READY -> TERMINATED): " +
                StateManager.transition(p, ProcessState.TERMINATED) + " state=" + p.getState());

        System.out.println("READY -> READY_SUSPENDED: " +
                StateManager.transition(p, ProcessState.READY_SUSPENDED) + " state=" + p.getState());

        System.out.println("READY_SUSPENDED -> READY: " +
                StateManager.transition(p, ProcessState.READY) + " state=" + p.getState());

        System.out.println();
    }

    private static void testMemoryAndGenerator() {
        System.out.println("=== 3) MEMORY + GENERATOR TEST ===");

        long cycle = 0;

        ProcessGenerator gen = new ProcessGenerator(12345L, 1);
        gen.setPriorityRange(1, 5);
        gen.setInstructionsRange(10, 60);
        gen.setDeadlineRange(50, 250);
        gen.setPeriodicity(30, 20, 80);
        gen.setTaskTypeChances(50, 40, 10);

        gen.setEmergencyConfig(
                1,
                5,
                25,
                5,
                25,
                TaskType.CPU_BOUND
        );

        MemoryManager mm = new MemoryManager(3, SuspensionPolicy.LOWEST_PRIORITY);

        System.out.println(">>> ADMIT INITIAL 5");
        admitAll(mm, gen.generateRandomBatch(5, cycle));

        System.out.println("\n>>> ADMIT BATCH 20");
        admitAll(mm, gen.generateRandomBatch(20, cycle));

        System.out.println("\n>>> ADMIT EMERGENCY 1");
        mm.admitProcess(gen.generateEmergencyProcess(cycle));

        System.out.println();
        printCoreSummary(mm);

        System.out.println();
    }

    private static void admitAll(MemoryManager mm, PCB[] procs) {
        for (int i = 0; i < procs.length; i++) {
            mm.admitProcess(procs[i]);
        }
    }

    private static void printCoreSummary(MemoryManager mm) {
        System.out.println("=== CORE SUMMARY ===");
        System.out.println("inRamCount=" + mm.getInRamCount() + "/max=" + mm.getMaxInRam());

        System.out.println("READY (RAM): " + queueNamesInline(mm.getReadyInRam()));
        System.out.println("BLOCKED (RAM): " + queueNamesInline(mm.getBlockedInRam()));
        System.out.println("READY_SUSPENDED: " + queueNamesInline(mm.getReadySuspended()));
        System.out.println("BLOCKED_SUSPENDED: " + queueNamesInline(mm.getBlockedSuspended()));

        System.out.println("\n=== SIZES ===");
        System.out.println("readyInRam.size=" + mm.getReadyInRam().size());
        System.out.println("blockedInRam.size=" + mm.getBlockedInRam().size());
        System.out.println("readySuspended.size=" + mm.getReadySuspended().size());
        System.out.println("blockedSuspended.size=" + mm.getBlockedSuspended().size());
    }

    private static String queueNamesInline(Queue<PCB> q) {
        if (q == null || q.isEmpty()) return "[]";

        Queue<PCB> tmp = new Queue<>();
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        int n = q.size();
        for (int i = 0; i < n; i++) {
            PCB p = q.dequeue();
            if (p == null) break;

            if (sb.length() > 1) sb.append(", ");
            sb.append(p.getName());

            tmp.enqueue(p);
        }

        int m = tmp.size();
        for (int i = 0; i < m; i++) {
            PCB p = tmp.dequeue();
            if (p == null) break;
            q.enqueue(p);
        }

        sb.append("]");
        return sb.toString();
    }
}

