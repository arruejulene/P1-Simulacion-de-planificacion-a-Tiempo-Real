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
        return readyQueue.dequeue();
    }

    @Override
    public int getQuantum() {
        return Integer.MAX_VALUE; // FCFS no usa quantum
    }
}
