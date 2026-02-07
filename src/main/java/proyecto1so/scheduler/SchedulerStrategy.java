/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package proyecto1so.scheduler;

/**
 *
 * @author ani
 */



import proyecto1so.datastructures.Queue;
import proyecto1so.model.Process;

public interface SchedulerStrategy {

    Process selectNextProcess(Queue<Process> readyQueue);

    
    int getQuantum();
}
