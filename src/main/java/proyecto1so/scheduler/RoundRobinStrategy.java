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

public class RoundRobinStrategy implements SchedulerStrategy {

    private final int quantum;

    public RoundRobinStrategy(int quantum) {
        this.quantum = quantum;
    }

    @Override
    public int getQuantum() {
        return quantum;
    }

    @Override
    public Process selectNextProcess(Queue<Process> readyQueue) {
        if (readyQueue.isEmpty()) return null;
        return readyQueue.dequeue();
    }
}
