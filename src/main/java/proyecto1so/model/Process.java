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

    
    private final int priority;

    
    private final int deadlineTick;

    private ProcessState state = ProcessState.NEW;

    private Integer firstRunTick = null;
    private Integer finishTick = null;

    
    public Process(String pid, int burstTime) {
        this(pid, burstTime, 0, 5, Integer.MAX_VALUE);
    }

    public Process(String pid, int burstTime, int arrivalTime) {
        this(pid, burstTime, arrivalTime, 5, Integer.MAX_VALUE);
    }

    
    public Process(String pid, int burstTime, int arrivalTime, int priority) {
        this(pid, burstTime, arrivalTime, priority, Integer.MAX_VALUE);
    }

    
    public Process(String pid, int burstTime, int arrivalTime, int priority, int deadlineTick) {
        this.pid = pid;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.priority = priority;
        this.deadlineTick = deadlineTick;
    }

    public String getPid() { return pid; }

    public int getBurstTime() { return burstTime; }

    public int getRemainingTime() { return remainingTime; }

    public int getArrivalTime() { return arrivalTime; }

    public int getPriority() { return priority; }

    public int getDeadlineTick() { return deadlineTick; }


    public int getDeadlineRemaining(int currentTick) {
        if (deadlineTick == Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return deadlineTick - currentTick;
    }

    public ProcessState getState() { return state; }

    public void setState(ProcessState state) { this.state = state; }

    public Integer getFirstRunTick() { return firstRunTick; }

    public Integer getFinishTick() { return finishTick; }

    public boolean isFinished() { return remainingTime <= 0; }

    public void consumeOneTick() {
        if (remainingTime > 0) remainingTime--;
    }

    public void markFirstRun(int tick) {
        if (firstRunTick == null) firstRunTick = tick;
    }

    public void markFinish(int tick) {
        finishTick = tick;
    }
}
