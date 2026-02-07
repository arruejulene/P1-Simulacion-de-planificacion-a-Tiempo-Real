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
import proyecto1so.scheduler.FCFSStrategy;

public class MainHotSwapStrategyTest {

    public static void main(String[] args) throws Exception {
        System.out.println("[HOT SWAP TEST] Iniciando reloj...");

        GlobalClock clock = new GlobalClock(300);

        CPUScheduler cpu = new CPUScheduler();
        clock.addListener(cpu);

     
        cpu.setStrategy(new FCFSStrategy());
        System.out.println("[HOT SWAP TEST] Estrategia inicial: FCFS");

        // Nota: tus procesos pueden crearse sin deadline (constructor de 4 args)
        cpu.addProcess(new Process("P1", 10, 1, 5));
        cpu.addProcess(new Process("P2", 6, 2, 4));
        cpu.addProcess(new Process("P3", 8, 3, 3));

        clock.start();


        Thread.sleep(2500);


        System.out.println("\n[HOT SWAP TEST] >>> CAMBIO EN CALIENTE A EDF <<<\n");
        cpu.setStrategy(new EDFStrategy());


        cpu.addProcess(new Process("EMERGENCY", 3, 1, 1, 6));

 
        Thread.sleep(6000);

        cpu.printReport();
        System.out.println("[HOT SWAP TEST] Fin.");
        System.exit(0);
    }
}
