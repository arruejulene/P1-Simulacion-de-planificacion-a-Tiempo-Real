/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1so.cpu;

/**
 *
 * @author ani
 */

import proyecto1so.clock.ClockListener;

public class CPUScheduler implements ClockListener {

    @Override
    public void onTick(int tick) {
        // TODO: lógica del scheduler por tick
        System.out.println("[CPU] Tick recibido: " + tick);
    }
}
