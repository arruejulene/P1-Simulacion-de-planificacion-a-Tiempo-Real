package proyecto1so.demo;

import proyecto1so.model.PCB;
import proyecto1so.model.TaskType;

public class MainPCB {
    public static void main(String[] args) {
        PCB p1 = new PCB(
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

        PCB p2 = new PCB(
                2,
                "P2",
                1,
                30,
                10,
                TaskType.IO_BOUND,
                true,
                15,
                15,
                0
        );

        PCB p3 = new PCB(
                3,
                "P3",
                5,
                25,
                8,
                TaskType.TRAP,
                false,
                0,
                0,
                0
        );

        System.out.println("=== INITIAL ===");
        System.out.println(p1);
        System.out.println(p2);
        System.out.println(p3);

        p1.decInstructionsRemaining();
        p1.decDeadlineRemaining();
        p1.incPc();
        p1.incMar();

        p2.decInstructionsRemaining();
        p2.decDeadlineRemaining();
        p2.incPc();
        p2.incMar();

        p3.decInstructionsRemaining();
        p3.decDeadlineRemaining();
        p3.incPc();
        p3.incMar();

        System.out.println("=== AFTER 1 TICK ===");
        System.out.println(p1);
        System.out.println(p2);
        System.out.println(p3);
    }
}
