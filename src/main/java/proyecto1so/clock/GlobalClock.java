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

    
    private final SingleLinkedList<ClockListener> listeners = new SingleLinkedList<>();

    public GlobalClock(long tickMillis) {
        this.tickMillis = tickMillis;
        setName("GlobalClock");
    }

    
    public void addListener(ClockListener listener) {
        if (listener == null) return;
        listeners.addLast(listener);
    }

    
    public long getCurrentTick() {
        return currentTick;
    }

    
    public void stopClock() {
        running = false;
        interrupt(); 
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(tickMillis);
            } catch (InterruptedException e) {
                
                if (!running) break;
            }

            
            if (!running) break;

            currentTick++;

           
            System.out.println("[CLOCK] Tick: " + currentTick);

            
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
