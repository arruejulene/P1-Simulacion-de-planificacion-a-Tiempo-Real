/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1so.model;

/**
 *
 * @author ani
 */

public class Process {

    private String id;
    private int remainingTime;

    public Process(String id, int time) {
        this.id = id;
        this.remainingTime = time;
    }

    public void executeTick() {
        remainingTime--;
        System.out.println("    [Process " + id + "] tiempo restante: " + remainingTime);
    }

    public boolean isFinished() {
        return remainingTime <= 0;
    }

    public String getId() {
        return id;
    }

    public int getRemainingTime() {
        return remainingTime;
    }
}
