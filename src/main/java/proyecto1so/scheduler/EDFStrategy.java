/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1so.scheduler;

/**
 *
 * @author ani
 */



import proyecto1so.datastructures.Compare;
import proyecto1so.datastructures.OrderedQueue;
import proyecto1so.datastructures.Queue;
import proyecto1so.model.Process;

public class EDFStrategy implements SchedulerStrategy {

    @Override
    public int getQuantum() {
        return Integer.MAX_VALUE; // EDF no usa quantum
    }

    @Override
    public Process selectNextProcess(Queue<Process> readyQueue) {
        if (readyQueue == null || readyQueue.isEmpty()) return null;

        OrderedQueue<Process> ordered = new OrderedQueue<>(new Compare<Process>() {
            @Override
            public int compare(Process a, Process b) {
                int diff = a.getDeadlineTick() - b.getDeadlineTick();
                if (diff != 0) return diff;
                return a.getPid().compareTo(b.getPid());
            }
        });

        // Pasar todo a ordered
        while (!readyQueue.isEmpty()) {
            ordered.insertOrdered(readyQueue.dequeue());
        }

        // Sacar el mejor
        Process next = ordered.dequeue();

        // ✅ IMPORTANTÍSIMO: devolver el resto a readyQueue para no perderlos
        while (!ordered.isEmpty()) {
            readyQueue.enqueue(ordered.dequeue());
        }

        return next;
    }
}
