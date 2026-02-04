/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1so.demo;

/**
 *
 * @author ani
 */

import proyecto1so.clock.GlobalClock;

public class MainClockTest {

    public static void main(String[] args) {
        // 1) Crear reloj con tick de 1000 ms (1 segundo)
        GlobalClock clock = new GlobalClock(1000);

        // 2) Iniciar el thread del reloj
        clock.start();

        // 3) Dejarlo correr unos segundos y luego detenerlo (opcional pero recomendado)
        try {
            Thread.sleep(6000); // deja correr ~6 ticks
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 4) Detener el reloj y mostrar el tick final
        clock.stopClock();
        System.out.println("[TEST] Clock stopped at tick: " + clock.getCurrentTick());
    }
}

