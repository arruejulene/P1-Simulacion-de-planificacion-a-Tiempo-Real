package proyecto1so.demo;

import proyecto1so.datastructures.Queue;
import proyecto1so.generator.ProcessGenerator;
import proyecto1so.memory.MemoryManager;
import proyecto1so.memory.SuspensionPolicy;
import proyecto1so.model.PCB;
import proyecto1so.model.ProcessState;
import proyecto1so.model.StateManager;

public class MainBlockedTest {
    public static void main(String[] args) {
        long cycle = 0;

        ProcessGenerator gen = new ProcessGenerator(12345L, 1);
        gen.setPriorityRange(1, 5);
        gen.setInstructionsRange(10, 60);
        gen.setDeadlineRange(50, 250);
        gen.setPeriodicity(30, 20, 80);
        gen.setTaskTypeChances(40, 50, 10);

        MemoryManager mm = new MemoryManager(3, SuspensionPolicy.LOWEST_PRIORITY);

        System.out.println("=== ADMIT 6 ===");
        PCB[] batch = gen.generateRandomBatch(6, cycle);
        for (int i = 0; i < batch.length; i++) {
            mm.admitProcess(batch[i]);
        }

        System.out.println();
        System.out.println("=== FORCE READY_IN_RAM -> BLOCKED_IN_RAM ===");
        moveAllReadyInRamToBlocked(mm);

        System.out.println();
        dumpCounts(mm);

        System.out.println();
        System.out.println("=== ADMIT 1 MORE (forces suspend; should suspend from BLOCKED_IN_RAM) ===");
        PCB extra = gen.generateRandomProcess(cycle);
        mm.admitProcess(extra);

        System.out.println();
        dumpCounts(mm);

        System.out.println();
        System.out.println("=== SIGNAL EVENT: BLOCKED_SUSPENDED -> READY_SUSPENDED ===");
        signalOneBlockedSuspended(mm);

        System.out.println();
        dumpCounts(mm);

        System.out.println();
        System.out.println("=== SWAP IN ONE: READY_SUSPENDED -> READY_IN_RAM ===");
        mm.swapInOneIfPossible();

        System.out.println();
        dumpCounts(mm);

        System.out.println();
        System.out.println("=== DUMP QUEUES (names only) ===");
        dumpQueueNames("readyInRam", mm.getReadyInRam());
        dumpQueueNames("blockedInRam", mm.getBlockedInRam());
        dumpQueueNames("readySuspended", mm.getReadySuspended());
        dumpQueueNames("blockedSuspended", mm.getBlockedSuspended());
    }

    private static void moveAllReadyInRamToBlocked(MemoryManager mm) {
        Queue<PCB> ready = mm.getReadyInRam();
        Queue<PCB> blocked = mm.getBlockedInRam();

        int n = ready.size();
        for (int i = 0; i < n; i++) {
            PCB p = ready.dequeue();
            if (p == null) break;

            StateManager.transition(p, ProcessState.RUNNING);
            StateManager.transition(p, ProcessState.BLOCKED);

            blocked.enqueue(p);
            System.out.println("[IO WAIT] " + p.getName() + " -> BLOCKED");
        }
    }

    private static void signalOneBlockedSuspended(MemoryManager mm) {
        PCB p = mm.getBlockedSuspended().dequeue();
        if (p == null) {
            System.out.println("[NO BLOCKED_SUSPENDED] nothing to signal");
            return;
        }

        StateManager.transition(p, ProcessState.READY_SUSPENDED);
        mm.getReadySuspended().enqueue(p);
        System.out.println("[EVENT] " + p.getName() + " BLOCKED_SUSPENDED -> READY_SUSPENDED");
    }

    private static void dumpCounts(MemoryManager mm) {
        System.out.println("=== COUNTS ===");
        System.out.println("maxInRam=" + mm.getMaxInRam());
        System.out.println("inRamCount=" + mm.getInRamCount());
        System.out.println("readyInRam.size=" + mm.getReadyInRam().size());
        System.out.println("blockedInRam.size=" + mm.getBlockedInRam().size());
        System.out.println("readySuspended.size=" + mm.getReadySuspended().size());
        System.out.println("blockedSuspended.size=" + mm.getBlockedSuspended().size());
    }

    private static void dumpQueueNames(String name, Queue<PCB> q) {
        System.out.println("=== " + name + " (size=" + q.size() + ") ===");
        Queue<PCB> tmp = new Queue<>();

        int n = q.size();
        for (int i = 0; i < n; i++) {
            PCB p = q.dequeue();
            if (p == null) break;
            System.out.println(p.getName() + " state=" + p.getState());
            tmp.enqueue(p);
        }

        int m = tmp.size();
        for (int i = 0; i < m; i++) {
            PCB p = tmp.dequeue();
            if (p == null) break;
            q.enqueue(p);
        }
    }
}
