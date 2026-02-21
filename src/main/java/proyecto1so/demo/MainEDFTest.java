/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1so.demo;



import proyecto1so.clock.GlobalClock;
import proyecto1so.cpu.CPUScheduler;
import proyecto1so.model.Process;
import proyecto1so.scheduler.EDFStrategy;

public class MainEDFTest {

    public static void main(String[] args) throws Exception {
        System.out.println("[EDF TEST] Iniciando reloj...");

        GlobalClock clock = new GlobalClock(300);

        CPUScheduler cpu = new CPUScheduler();
        clock.addListener(cpu);


        cpu.setStrategy(new EDFStrategy());


        cpu.addProcess(new Process("P1", 8, 1, 5, 20));
        cpu.addProcess(new Process("P2", 3, 3, 1, 6));   // deadline más cercano -> preempta
        cpu.addProcess(new Process("P3", 4, 6, 3, 12));

     
        clock.start();


        Thread.sleep(7000);

    
        cpu.printReport();
        System.out.println("[EDF TEST] Fin.");
        System.exit(0);
    }
}
