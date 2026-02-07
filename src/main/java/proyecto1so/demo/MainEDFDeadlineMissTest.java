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


public class MainEDFDeadlineMissTest {

    public static void main(String[] args) throws Exception {

        System.out.println("[EDF MISS TEST] Iniciando reloj...");

        GlobalClock clock = new GlobalClock(300);

        CPUScheduler cpu = new CPUScheduler();
        clock.addListener(cpu);

        cpu.setStrategy(new EDFStrategy());

        cpu.addProcess(new Process("P1", 10, 1, 1, 5));

        clock.start();


        Thread.sleep(6000);

        cpu.printReport();
        System.out.println("[EDF MISS TEST] Fin.");
        System.exit(0);
    }
}
