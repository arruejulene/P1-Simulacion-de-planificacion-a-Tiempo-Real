/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1so.demo;

/**
 *
 * @author ani
 */

import proyecto1so.clock.ClockListener;
import proyecto1so.clock.GlobalClock;

public class MainClockTest {

    public static void main(String[] args) throws InterruptedException {

        
        GlobalClock clock = new GlobalClock(500);

        
        clock.addListener(new ClockListener() {
            @Override
            public void onTick(int tick) {
                System.out.println("[LISTENER-1] Tick recibido: " + tick);

                
                if (tick == 5) {
                    System.out.println("[TEST] Pidiendo stopClock() en tick: " + tick);
                    clock.stopClock();
                }
            }
        });

        
        clock.addListener(new ClockListener() {
            @Override
            public void onTick(int tick) {
                System.out.println("[LISTENER-2] Tick recibido: " + tick);
            }
        });

        
        clock.start();

        
        clock.join();

        System.out.println("[TEST] Clock thread terminó. Fin de prueba.");
    }
}

