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
    public int getQuantum() {
        return Integer.MAX_VALUE; 
    }

    @Override
    public Process selectNextProcess(Queue<Process> readyQueue) {
        if (readyQueue.isEmpty()) return null;
        return readyQueue.dequeue();
    }
}
