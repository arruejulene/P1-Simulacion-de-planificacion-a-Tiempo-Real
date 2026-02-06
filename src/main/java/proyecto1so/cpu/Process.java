/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1so.cpu;

/**
 *
 * @author ani
 */

public class Process {

    private final String id;
    private int remainingTime;

    public Process(String id, int burstTime) {
        this.id = id;
        this.remainingTime = burstTime;
    }

    public String getId() {
        return id;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void executeOneTick() {
        if (remainingTime > 0) {
            remainingTime--;
        }
    }

    public boolean isFinished() {
        return remainingTime <= 0;
    }

    @Override
    public String toString() {
        return "Process{" +
                "id='" + id + '\'' +
                ", remainingTime=" + remainingTime +
                '}';
    }
}
