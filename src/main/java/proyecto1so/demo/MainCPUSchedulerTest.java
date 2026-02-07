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
import proyecto1so.scheduler.PriorityPreemptiveStrategy;

public class MainCPUSchedulerTest {

    public static void main(String[] args) throws Exception {
        System.out.println("[TEST] Iniciando reloj...");

        GlobalClock clock = new GlobalClock(300);

        CPUScheduler cpu = new CPUScheduler();
        clock.addListener(cpu);

        cpu.setStrategy(new PriorityPreemptiveStrategy());

        // (pid, burst, arrival, priority)
        cpu.addProcess(new Process("P1", 8, 1, 5));
        cpu.addProcess(new Process("P2", 3, 3, 1));
        cpu.addProcess(new Process("P3", 4, 5, 3));

        clock.start();

        // deja correr unos segundos
        Thread.sleep(6500);

        // imprime reporte y termina
        cpu.printReport();
        System.out.println("[TEST] Fin.");
        System.exit(0);
    }
}
