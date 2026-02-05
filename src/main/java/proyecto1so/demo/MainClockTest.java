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

        // Tick cada 500ms (0.5s). Cambia este valor si quieres más rápido/lento.
        GlobalClock clock = new GlobalClock(500);

        // Listener 1
        clock.addListener(new ClockListener() {
            @Override
            public void onTick(int tick) {
                System.out.println("[LISTENER-1] Tick recibido: " + tick);

                // Detenemos en el tick 5
                if (tick == 5) {
                    System.out.println("[TEST] Pidiendo stopClock() en tick: " + tick);
                    clock.stopClock();
                }
            }
        });

        // Listener 2 (solo para probar que soporta múltiples listeners)
        clock.addListener(new ClockListener() {
            @Override
            public void onTick(int tick) {
                System.out.println("[LISTENER-2] Tick recibido: " + tick);
            }
        });

        // Iniciar el reloj (Thread)
        clock.start();

        // Esperar a que el thread termine (cuando se llame stopClock)
        clock.join();

        System.out.println("[TEST] Clock thread terminó. Fin de prueba.");
    }
}

