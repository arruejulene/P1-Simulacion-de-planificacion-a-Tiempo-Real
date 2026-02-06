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

public class MainCPUSchedulerTest {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("[TEST] Iniciando reloj...");

        GlobalClock clock = new GlobalClock(500); // si te da error, me dices qué constructor tiene tu GlobalClock
        CPUScheduler cpuScheduler = new CPUScheduler();

        clock.addListener(cpuScheduler);

        clock.start();

        while (clock.getCurrentTick() < 5) {
            Thread.sleep(50);
        }

        System.out.println("[TEST] Deteniendo reloj...");
        clock.stopClock();
        clock.join();

        System.out.println("[TEST] Fin.");
    }
}
