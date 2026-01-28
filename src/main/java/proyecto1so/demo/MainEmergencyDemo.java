package proyecto1so.demo;

import proyecto1so.datastructures.Queue;
import proyecto1so.generator.ProcessGenerator;
import proyecto1so.memory.MemoryManager;
import proyecto1so.memory.SuspensionPolicy;
import proyecto1so.model.PCB;
import proyecto1so.model.TaskType;

public class MainEmergencyDemo {
    public static void main(String[] args) {
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

        System.out.println("=== INITIAL 5 ===");
        admitAll(mm, gen.generateRandomBatch(5, cycle));

        System.out.println();
        System.out.println("=== BATCH 20 ===");
        admitAll(mm, gen.generateRandomBatch(20, cycle));

        System.out.println();
        System.out.println("=== EMERGENCY 1 ===");
        mm.admitProcess(gen.generateEmergencyProcess(cycle));

        System.out.println();
        System.out.println("=== COUNTS ===");
        System.out.println("maxInRam=" + mm.getMaxInRam());
        System.out.println("inRamCount=" + mm.getInRamCount());
        System.out.println("readyInRam.size=" + mm.getReadyInRam().size());
        System.out.println("blockedInRam.size=" + mm.getBlockedInRam().size());
        System.out.println("readySuspended.size=" + mm.getReadySuspended().size());
        System.out.println("blockedSuspended.size=" + mm.getBlockedSuspended().size());

        System.out.println();
        dumpQueueNames("readyInRam", mm.getReadyInRam());
        dumpQueueNames("readySuspended", mm.getReadySuspended());
    }

    private static void admitAll(MemoryManager mm, PCB[] procs) {
        for (int i = 0; i < procs.length; i++) {
            mm.admitProcess(procs[i]);
        }
    }

    private static void dumpQueueNames(String name, Queue<PCB> q) {
        System.out.println("=== " + name + " (size=" + q.size() + ") ===");
        Queue<PCB> tmp = new Queue<>();

        int n = q.size();
        for (int i = 0; i < n; i++) {
            PCB p = q.dequeue();
            if (p == null) break;
            System.out.println(p.getName() + " id=" + p.getId() + " prio=" + p.getPriority() + " ddl=" + p.getDeadlineRemaining());
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
