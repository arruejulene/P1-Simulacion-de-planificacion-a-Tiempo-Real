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

    // ✅ Reglas del PDF: estados + PC/MAR
    private ProcessState state;
    private int pc;
    private int mar;

    public Process(String pid, int burstTime) {
        this(pid, burstTime, 0);
    }

    public Process(String pid, int burstTime, int arrivalTime) {
        this.pid = pid;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.arrivalTime = arrivalTime;

        // Estado inicial obligatorio
        this.state = ProcessState.NEW;

        // Simplificación: arrancan en 0
        this.pc = 0;
        this.mar = 0;
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

    // ✅ Estado
    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    // ✅ PC/MAR
    public int getPc() {
        return pc;
    }

    public int getMar() {
        return mar;
    }

    public boolean isFinished() {
        return remainingTime <= 0;
    }

    // ✅ Consume 1 tick de CPU (1 instrucción = 1 ciclo) y aumenta PC/MAR
    public void consumeOneTick() {
        if (remainingTime > 0) {
            remainingTime--;
            pc++;
            mar++;
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
