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

        GlobalClock clock = new GlobalClock(500); // 500 ms por tick
        CPUScheduler cpu = new CPUScheduler();

        // ✅ Agregar procesos ANTES de iniciar el reloj
        cpu.addProcess(new Process("P1", 3));
        cpu.addProcess(new Process("P2", 5));
        cpu.addProcess(new Process("P3", 2));

        // ✅ Conectar CPU al reloj
        clock.addListener(cpu);

        System.out.println("[TEST] Iniciando reloj...");
        clock.start();

        // Deja correr varios ticks para ver cambios
        while (clock.getCurrentTick() < 15) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) { }
        }

        System.out.println("[TEST] Deteniendo reloj...");
        clock.stopClock();

        try {
            clock.join();
        } catch (InterruptedException e) { }

        System.out.println("[TEST] Fin.");
    }
}
