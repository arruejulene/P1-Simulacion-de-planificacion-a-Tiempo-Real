package proyecto1so.demo;

import proyecto1so.generator.ProcessGenerator;
import proyecto1so.memory.MemoryManager;
import proyecto1so.memory.SuspensionPolicy;
import proyecto1so.model.PCB;

public class MainGenerator {
    public static void main(String[] args) {
        long cycle = 0;

        ProcessGenerator gen = new ProcessGenerator(12345L, 1);
        gen.setPriorityRange(1, 5);
        gen.setInstructionsRange(10, 60);
        gen.setDeadlineRange(50, 250);
        gen.setPeriodicity(30, 20, 80);
        gen.setTaskTypeChances(50, 40, 10);

        MemoryManager mm = new MemoryManager(3, SuspensionPolicy.LOWEST_PRIORITY);

        PCB[] batch = gen.generateRandomBatch(20, cycle);
        for (int i = 0; i < batch.length; i++) {
            mm.admitProcess(batch[i]);
        }

        System.out.println();
        System.out.println("=== FINAL COUNTS ===");
        System.out.println("maxInRam=" + mm.getMaxInRam());
        System.out.println("inRamCount=" + mm.getInRamCount());
        System.out.println("readyInRam.size=" + mm.getReadyInRam().size());
        System.out.println("readySuspended.size=" + mm.getReadySuspended().size());
        System.out.println("blockedInRam.size=" + mm.getBlockedInRam().size());
        System.out.println("blockedSuspended.size=" + mm.getBlockedSuspended().size());
    }
}
