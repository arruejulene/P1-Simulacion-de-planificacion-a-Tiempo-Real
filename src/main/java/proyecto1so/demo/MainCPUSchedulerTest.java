/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1so.demo;

/**
 *
 * @author ani
 */





import proyecto1so.clock.GlobalClock;
import proyecto1so.cpu.CPUScheduler;
import proyecto1so.model.Process;
import proyecto1so.scheduler.EDFStrategy;

public class MainCPUSchedulerTest {

    public static void main(String[] args) throws Exception {
        System.out.println("[TEST] Iniciando reloj...");

        GlobalClock clock = new GlobalClock(300);
        CPUScheduler cpu = new CPUScheduler();
        clock.addListener(cpu);

        // EDF
        cpu.setStrategy(new EDFStrategy());

        // (pid, burst, arrival, priority, deadlineTick)
        // EDF elige menor deadlineTick y PREEMPTA si llega uno más urgente
        cpu.addProcess(new Process("P1", 8, 1, 5, 20)); // deadline lejano
        cpu.addProcess(new Process("P2", 3, 3, 5, 8));  // deadline cercano -> debe preemptar
        cpu.addProcess(new Process("P3", 4, 5, 5, 12)); // medio

        clock.start();

        Thread.sleep(12000);

        cpu.printReport();
        System.out.println("[TEST] Fin.");
        System.exit(0);
    }
}
