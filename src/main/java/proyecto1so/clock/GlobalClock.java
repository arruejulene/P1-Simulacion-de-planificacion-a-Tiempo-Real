/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1so.clock;

/**
 *
 * @author ani
 */


import java.util.concurrent.Semaphore;
import proyecto1so.datastructures.Queue;

public class GlobalClock extends Thread {

    private volatile int tickMillis;

    private volatile boolean running = true;
    private int currentTick = 0;

    private final Semaphore mutex = new Semaphore(1, true);
    private final Queue<ClockListener> listeners = new Queue<>();

    public GlobalClock(int tickMillis) {
        this.tickMillis = tickMillis;
        setName("GlobalClock");
    }

    public void setTickMillis(int tickMillis) {
        if (tickMillis > 0) this.tickMillis = tickMillis;
    }

    public int getTickMillis() {
        return tickMillis;
    }

    public void addListener(ClockListener listener) {
        if (listener == null) return;
        boolean acquired = false;
        try {
            mutex.acquire();
            acquired = true;
            listeners.enqueue(listener);
        } catch (InterruptedException e) {
      
        } finally {
            if (acquired) mutex.release();
        }
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void stopClock() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(tickMillis);
            } catch (InterruptedException e) {
           
            }

            currentTick++;
            System.out.println("[CLOCK] Tick: " + currentTick);

            
            try {
                mutex.acquire();

                int n = listeners.size();
                for (int i = 0; i < n; i++) {
                    ClockListener l = listeners.dequeue();
                
                    listeners.enqueue(l);

                    if (l != null) {
                        l.onTick(currentTick);
                    }
                }
            } catch (InterruptedException e) {
            
            } finally {
                mutex.release();
            }
        }
    }
}
