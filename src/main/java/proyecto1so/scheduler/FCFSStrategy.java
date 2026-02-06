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

public class FCFSStrategy implements SchedulerStrategy {

    @Override
    public Process selectNextProcess(Queue<Process> readyQueue) {
        
        return readyQueue.poll();
    }

    @Override
    public int getQuantum() {
        
        return 0;
    }

    @Override
    public void onQuantumExpired(Process p, Queue<Process> readyQueue) {
        
    }

    @Override
    public void onProcessFinished(Process p) {
        
    }
}
