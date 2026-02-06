/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1so.cpu;

/**
 *
 * @author ani
 */



import java.util.ArrayDeque;
import java.util.Queue;
import proyecto1so.clock.ClockListener;
import proyecto1so.model.Process;

public class CPUScheduler implements ClockListener {

    private final Queue<Process> readyQueue = new ArrayDeque<>();
    private Process running = null;

    private final int quantum = 2;     // puedes cambiarlo
    private int quantumLeft = 0;

    public void addProcess(Process p) {
        readyQueue.add(p);
    }

    @Override
    public void onTick(int tick) {
        // Si no hay proceso corriendo, tomar uno de la cola
        if (running == null) {
            running = readyQueue.poll();
            if (running != null) {
                quantumLeft = quantum;
                System.out.println("[CPU] Tick " + tick + " -> Ejecutando: " + running.getId());
            } else {
                System.out.println("[CPU] Tick " + tick + " -> IDLE (sin procesos)");
                return;
            }
        }

        // Ejecutar 1 tick del proceso
        running.executeTick();
        quantumLeft--;

        System.out.println("[CPU] Tick " + tick
                + " -> " + running.getId()
                + " restante=" + running.getRemainingTime()
                + " quantumLeft=" + quantumLeft);

        // Si terminó, liberar CPU
        if (running.isFinished()) {
            System.out.println("[CPU] " + running.getId() + " terminó.");
            running = null;
            return;
        }

        // Si se acabó el quantum, reencolar
        if (quantumLeft <= 0) {
            System.out.println("[CPU] Quantum terminado para " + running.getId() + " -> reencolando");
            readyQueue.add(running);
            running = null;
        }
    }
}
