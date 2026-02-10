package proyecto1so.demo;

import proyecto1so.clock.GlobalClock;
import proyecto1so.cpu.CPUScheduler;
import proyecto1so.io.IOEventGenerator;
import proyecto1so.loader.LoadResult;
import proyecto1so.loader.ProcessLoader;
import proyecto1so.memory.SuspensionPolicy;
import proyecto1so.model.Process;
import proyecto1so.scheduler.RoundRobinStrategy;

public class MainIOEventsTest {

    public static void main(String[] args) throws Exception {
        System.out.println("[IO TEST] Iniciando prueba de E/S...");
        String jsonPath = args.length > 0 ? args[0] : "sample-data/processes.json";

        int tickMs = 300;
        GlobalClock clock = new GlobalClock(tickMs);

        CPUScheduler cpu = new CPUScheduler(5, SuspensionPolicy.LOWEST_PRIORITY);
        cpu.setTickDurationMs(tickMs);
        cpu.setStrategy(new RoundRobinStrategy(2));
        clock.addListener(cpu);

        ProcessLoader loader = new ProcessLoader();
        LoadResult loadResult = loader.loadFromJson(jsonPath);
        Process[] loaded = loadResult.getProcesses();
        String[] errors = loadResult.getErrors();

        System.out.println("[IO TEST] JSON source: " + jsonPath);
        for (int i = 0; i < errors.length; i++) {
            System.out.println("[IO TEST][JSON ERROR] " + errors[i]);
        }

        if (loaded.length > 0) {
            for (int i = 0; i < loaded.length; i++) {
                cpu.addProcess(loaded[i]);
            }
            System.out.println("[IO TEST] Procesos cargados desde JSON: " + loaded.length);
        } else {
            System.out.println("[IO TEST] JSON sin procesos válidos, usando dataset de respaldo");
            cpu.addProcess(new Process("P1", 16, 1, 3, 40));
            cpu.addProcess(new Process("P2", 12, 2, 2, 35));
            cpu.addProcess(new Process("P3", 10, 3, 1, 30));
            cpu.addProcess(new Process("P4", 8, 4, 4, 30));
            cpu.addProcess(new Process("P5", 7, 5, 5, 60));
            cpu.addProcess(new Process("P6", 6, 6, 5, 70));
        }

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
