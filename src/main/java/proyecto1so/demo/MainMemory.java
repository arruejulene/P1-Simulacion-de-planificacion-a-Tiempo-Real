package proyecto1so.demo;

import proyecto1so.memory.MemoryManager;
import proyecto1so.model.PCB;
import proyecto1so.model.TaskType;

public class MainMemory {
    public static void main(String[] args) {
        MemoryManager mm = new MemoryManager(3);

        for (int i = 1; i <= 6; i++) {
            PCB p = new PCB(
                    i,
                    "P" + i,
                    1 + (i % 5),
                    50,
                    10,
                    TaskType.CPU_BOUND,
                    false,
                    0,
                    0,
                    0
            );
            boolean inRam = mm.admitProcess(p);
            System.out.println("admit P" + i + " inRam=" + inRam + " inRamCount=" + mm.getInRamCount());
        }

        System.out.println("suspendOne=" + mm.suspendOne() + " inRamCount=" + mm.getInRamCount());
        System.out.println("swapInOneIfPossible=" + mm.swapInOneIfPossible() + " inRamCount=" + mm.getInRamCount());
    }
}
