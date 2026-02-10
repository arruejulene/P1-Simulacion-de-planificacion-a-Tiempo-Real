package proyecto1so.demo;

import proyecto1so.clock.GlobalClock;
import proyecto1so.cpu.CPUScheduler;
import proyecto1so.interrupts.InterruptGenerator;
import proyecto1so.loader.LoadResult;
import proyecto1so.loader.ProcessLoader;
import proyecto1so.model.Process;
import proyecto1so.scheduler.FCFSStrategy;
import proyecto1so.memory.SuspensionPolicy;

public class MainInterruptionsTest {

    public static void main(String[] args) throws Exception {
        System.out.println("[INT TEST] Iniciando prueba de interrupciones...");
        
        
        String jsonPath = args.length > 0 ? args[0] : "sample-data/processes.json";


        GlobalClock clock = new GlobalClock(300);
        
        CPUScheduler cpu = new CPUScheduler(5, SuspensionPolicy.LOWEST_PRIORITY);
        cpu.setStrategy(new FCFSStrategy());
        clock.addListener(cpu);

        
        ProcessLoader loader = new ProcessLoader();
        try {
            LoadResult loadResult = loader.loadFromJson(jsonPath);
            Process[] loaded = loadResult.getProcesses();
            String[] errors = loadResult.getErrors();

            if (errors.length > 0) {
                for (String error : errors) {
                    System.err.println("[INT TEST][JSON ERROR] " + error);
                }
            }

            for (Process p : loaded) {
                cpu.addProcess(p);
                System.out.println("[INT TEST] Proceso cargado desde JSON: " + loaded.length);
            }
        } catch (Exception e) {
            System.err.println("[INT TEST] No se pudo cargar el JSON: " + e.getMessage());
        }

       
        System.out.println("[INT TEST] Cargando procesos críticos de control...");
        cpu.addProcess(new Process("ADCS-Ctrl", 14, 1, 10, 30)); // Prioridad alta (10)
        cpu.addProcess(new Process("OBDH-Data", 10, 2, 5, 30));
        cpu.addProcess(new Process("COMMS-Link", 8, 4, 8, 25));

        
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

        Thread.sleep(10000); // 10 segundos

        
        generator.stopGenerator();
        clock.stopClock();
        generator.join();
        clock.join();

        
        cpu.printReport();
        System.out.println("[INT TEST] Fin de la misión.");
    }
}