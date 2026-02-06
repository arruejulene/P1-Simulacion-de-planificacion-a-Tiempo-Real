/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1so.cpu;

/**
 *
 * @author ani
 */


import java.util.LinkedList;
import java.util.Queue;
import proyecto1so.clock.ClockListener;

public class CPUScheduler implements ClockListener {

    private Queue<Process> readyQueue;
    private Process currentProcess;

    public CPUScheduler() {
        this.readyQueue = new LinkedList<>();
        this.currentProcess = null;
    }

    public void addProcess(Process process) {
        readyQueue.add(process);
        System.out.println("[CPU] Proceso agregado: " + process);
    }

    @Override
    public void onTick(int tick) {
        System.out.println("[CPU] Tick recibido: " + tick);

        if (currentProcess == null && !readyQueue.isEmpty()) {
            currentProcess = readyQueue.poll();
            System.out.println("[CPU] Ejecutando proceso: " + currentProcess.getId());
        }

        if (currentProcess != null) {
            currentProcess.executeOneTick();

            if (currentProcess.isFinished()) {
                System.out.println("[CPU] Proceso terminado: " + currentProcess.getId());
                currentProcess = null;
            }
        }
    }
}
