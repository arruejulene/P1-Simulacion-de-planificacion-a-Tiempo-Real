package proyecto1so.demo;

import proyecto1so.clock.GlobalClock;
import proyecto1so.cpu.CPUScheduler;
import proyecto1so.interrupts.InterruptGenerator;
import proyecto1so.model.Process;
import proyecto1so.scheduler.FCFSStrategy;

public class MainInterruptionsTest {

    public static void main(String[] args) throws Exception {
        System.out.println("[INT TEST] Iniciando prueba de interrupciones...");

        GlobalClock clock = new GlobalClock(300);
        CPUScheduler cpu = new CPUScheduler();
        cpu.setStrategy(new FCFSStrategy());
        clock.addListener(cpu);

        cpu.addProcess(new Process("P1", 14, 1, 4, 30));
        cpu.addProcess(new Process("P2", 10, 2, 3, 30));
        cpu.addProcess(new Process("P3", 8, 4, 2, 25));
        cpu.addProcess(new Process("P4", 6, 6, 1, 20));

        InterruptGenerator generator = new InterruptGenerator(
                cpu,
                900,
                1800,
                1,
                2,
                12345L
        );

        clock.start();
        generator.start();

        Thread.sleep(10000);

        generator.stopGenerator();
        clock.stopClock();
        generator.join();
        clock.join();

        cpu.printReport();
        System.out.println("[INT TEST] Fin.");
    }
}
