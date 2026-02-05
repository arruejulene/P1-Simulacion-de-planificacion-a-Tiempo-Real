/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1so.clock;

/**
 *
 * @author ani
 */

import proyecto1so.datastructures.SingleLinkedList;

public class GlobalClock extends Thread {

    private final long tickMillis;
    private volatile boolean running = true;
    private volatile long currentTick = 0;

    // Lista de "suscriptores" que quieren enterarse de cada tick
    private final SingleLinkedList<ClockListener> listeners = new SingleLinkedList<>();

    public GlobalClock(long tickMillis) {
        this.tickMillis = tickMillis;
        setName("GlobalClock");
    }

    /**
     * Registra un listener para que reciba onTick(tick) en cada tick del reloj.
     */
    public void addListener(ClockListener listener) {
        if (listener == null) return;
        listeners.addLast(listener);
    }

    /**
     * Devuelve el tick actual.
     */
    public long getCurrentTick() {
        return currentTick;
    }

    /**
     * Detiene el reloj.
     */
    public void stopClock() {
        running = false;
        interrupt(); // despierta el sleep si está durmiendo
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(tickMillis);
            } catch (InterruptedException e) {
                // Si nos interrumpen para detener, salimos limpio
                if (!running) break;
            }

            // Chequeo extra para evitar "tick fantasma"
            if (!running) break;

            currentTick++;

            // (Opcional) Log del reloj
            System.out.println("[CLOCK] Tick: " + currentTick);

            // Notificar a todos los listeners
            final int tickInt = (int) currentTick;
            listeners.forEach(new SingleLinkedList.Visitor<ClockListener>() {
                @Override
                public void visit(ClockListener l) {
                    l.onTick(tickInt);
                }
            });
        }
    }
}
