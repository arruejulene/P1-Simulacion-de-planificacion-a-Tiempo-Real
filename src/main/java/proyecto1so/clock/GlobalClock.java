/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1so.clock;

/**
 *
 * @author ani
 */

public class GlobalClock extends Thread {


    private final long tickMillis;
    private volatile boolean running = true;
    private volatile long currentTick = 0;

    public GlobalClock(long tickMillis) {
        this.tickMillis = tickMillis;
        setName("GlobalClock");
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
            System.out.println("[CLOCK] Tick: " + currentTick);
        }
    }

    public void stopClock() {
        running = false;
        interrupt(); // despierta el sleep
    }

    public long getCurrentTick() {
        return currentTick;
    }
}
