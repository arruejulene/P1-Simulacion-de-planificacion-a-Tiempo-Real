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

public class MainCPUSchedulerTest {

    public static void main(String[] args) {

        GlobalClock clock = new GlobalClock(500);
        CPUScheduler cpu = new CPUScheduler();

        cpu.addProcess(new Process("P1", 3, 1));
        cpu.addProcess(new Process("P2", 5, 3));
        cpu.addProcess(new Process("P3", 2, 5));

        clock.addListener(cpu);

        System.out.println("[TEST] Iniciando reloj...");
        clock.start();

        // Espera hasta que termine todo (con un límite de seguridad)
        while (!cpu.isAllDone() && clock.getCurrentTick() < 200) {
            try { Thread.sleep(50); } catch (InterruptedException e) { }
        }

        System.out.println("[TEST] Deteniendo reloj...");
        clock.stopClock();

        try { clock.join(); } catch (InterruptedException e) { }

        // Reporte final
        cpu.printReport();

        System.out.println("[TEST] Fin.");
    }
}
