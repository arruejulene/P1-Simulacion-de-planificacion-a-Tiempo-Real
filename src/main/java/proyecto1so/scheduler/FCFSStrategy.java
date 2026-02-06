/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1so.scheduler;

/**
 *
 * @author ani
 */


import proyecto1so.datastructures.Queue;
import proyecto1so.model.Process;

public class FCFSStrategy implements SchedulerStrategy {

    @Override
    public Process selectNextProcess(Queue<Process> readyQueue) {
        return readyQueue.dequeue(); // primero en llegar, primero en salir
    }

    @Override
    public int getQuantum() {
        return 0; // FCFS no usa quantum
    }

    @Override
    public void onQuantumExpired(Process p, Queue<Process> readyQueue) {
        // No aplica en FCFS
    }

    @Override
    public void onProcessFinished(Process p) {
        // No requiere nada especial
    }
}
