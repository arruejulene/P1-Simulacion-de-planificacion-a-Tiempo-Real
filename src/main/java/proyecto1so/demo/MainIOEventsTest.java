package proyecto1so.demo;

import proyecto1so.clock.GlobalClock;
import proyecto1so.cpu.CPUScheduler;
import proyecto1so.io.IOEventGenerator;
import proyecto1so.memory.SuspensionPolicy;
import proyecto1so.model.Process;
import proyecto1so.scheduler.RoundRobinStrategy;

public class MainIOEventsTest {

    public static void main(String[] args) throws Exception {
        System.out.println("[IO TEST] Iniciando prueba de E/S...");

        int tickMs = 300;
        GlobalClock clock = new GlobalClock(tickMs);

        CPUScheduler cpu = new CPUScheduler(3, SuspensionPolicy.LOWEST_PRIORITY);
        cpu.setTickDurationMs(tickMs);
        cpu.setStrategy(new RoundRobinStrategy(2));
        clock.addListener(cpu);

        cpu.addProcess(new Process("P1", 16, 1, 3, 40));
        cpu.addProcess(new Process("P2", 12, 2, 2, 35));
        cpu.addProcess(new Process("P3", 10, 3, 1, 30));
        cpu.addProcess(new Process("P4", 8, 4, 4, 30));
        cpu.addProcess(new Process("P5", 7, 5, 5, 60));
        cpu.addProcess(new Process("P6", 6, 6, 5, 70));

        IOEventGenerator ioGen = new IOEventGenerator(
                cpu,
                700,
                1500,
                1,
                3,
                54321L
        );

        clock.start();
        ioGen.start();

        Thread.sleep(10000);

        ioGen.stopGenerator();
        clock.stopClock();
        ioGen.join();
        clock.join();

        cpu.printReport();
        System.out.println("[IO TEST] Fin.");
    }
}
