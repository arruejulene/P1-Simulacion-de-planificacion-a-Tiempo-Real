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

    private final String pid;
    private final int burstTime;
    private int remainingTime;


    private final int arrivalTime;


    private Integer firstRunTick = null;
    private Integer finishTick = null;

    
    public Process(String pid, int burstTime) {
        this(pid, burstTime, 0);
    }


    public Process(String pid, int burstTime, int arrivalTime) {
        this.pid = pid;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.arrivalTime = arrivalTime;
    }

    public String getPid() {
        return pid;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public Integer getFirstRunTick() {
        return firstRunTick;
    }

    public Integer getFinishTick() {
        return finishTick;
    }

    public boolean isFinished() {
        return remainingTime <= 0;
    }

    // Consume 1 tick de CPU
    public void consumeOneTick() {
        if (remainingTime > 0) {
            remainingTime--;
        }
    }

    public void markFirstRun(int tick) {
        if (firstRunTick == null) {
            firstRunTick = tick;
        }
    }

    public void markFinish(int tick) {
        finishTick = tick;
    }
}
