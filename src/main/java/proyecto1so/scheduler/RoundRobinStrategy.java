/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1so.scheduler;

/**
 *
 * @author ani
 */

import java.util.Queue;
import proyecto1so.model.Process;

public class RoundRobinStrategy implements SchedulerStrategy {

    private final int quantum;

    public RoundRobinStrategy(int quantum) {
        this.quantum = quantum;
    }

    @Override
    public Process selectNextProcess(Queue<Process> readyQueue) {
        return readyQueue.poll();
    }

    @Override
    public int getQuantum() {
        return quantum;
    }

    @Override
    public void onQuantumExpired(Process p, Queue<Process> readyQueue) {
        // RR: reencola al final
        readyQueue.add(p);
    }

    @Override
    public void onProcessFinished(Process p) {
        // RR no necesita nada aquí
    }
}

