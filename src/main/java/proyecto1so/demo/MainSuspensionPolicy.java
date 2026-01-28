package proyecto1so.demo;

import proyecto1so.memory.MemoryManager;
import proyecto1so.memory.SuspensionPolicy;
import proyecto1so.model.PCB;
import proyecto1so.model.TaskType;

public class MainSuspensionPolicy {
    public static void main(String[] args) {
        MemoryManager mm = new MemoryManager(3, SuspensionPolicy.LOWEST_PRIORITY);

        for (int i = 1; i <= 10; i++) {
            PCB p = new PCB(
                    i,
                    "P" + i,
                    (i % 5) + 1,
                    10 + (i * 7),
                    10,
                    TaskType.CPU_BOUND,
                    false,
                    0,
                    0,
                    0
            );
            mm.admitProcess(p);
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
