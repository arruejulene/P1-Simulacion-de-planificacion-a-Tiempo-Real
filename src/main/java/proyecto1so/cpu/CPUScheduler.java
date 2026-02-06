/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1so.cpu;

/**
 *
 * @author ani
 */


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import proyecto1so.clock.ClockListener;
import proyecto1so.model.Process;
import proyecto1so.scheduler.RoundRobinStrategy;
import proyecto1so.scheduler.SchedulerStrategy;

public class CPUScheduler implements ClockListener {

    private final Queue<Process> readyQueue = new ArrayDeque<>();
    private final List<Process> pendingArrivals = new ArrayList<>();
    private final List<Process> allProcesses = new ArrayList<>();

    private Process currentProcess = null;
    private int quantumLeft = 0;

    private int totalTicks = 0;
    private int busyTicks = 0;

    // ✅ NUEVO: Strategy actual
    private SchedulerStrategy strategy = new RoundRobinStrategy(2); // quantum=2 por defecto

    // ✅ Permite cambiar algoritmo en runtime (más adelante lo usas en caliente)
    public void setStrategy(SchedulerStrategy strategy) {
        if (strategy == null) return;
        this.strategy = strategy;

        // Si hay proceso corriendo, definimos qué pasa:
        // opción simple: mantener el proceso actual pero resetear quantum
        if (currentProcess != null) {
            quantumLeft = this.strategy.getQuantum();
        }
    }

    public SchedulerStrategy getStrategy() {
        return strategy;
    }

    public void addProcess(Process p) {
        pendingArrivals.add(p);
        pendingArrivals.sort(Comparator.comparingInt(Process::getArrivalTime));
        allProcesses.add(p);
    }

    private void enqueueArrivals(int tick) {
        Iterator<Process> it = pendingArrivals.iterator();
        while (it.hasNext()) {
            Process p = it.next();
            if (p.getArrivalTime() <= tick) {
                readyQueue.add(p);
                System.out.println("[CPU] Tick " + tick + " -> Llega: " + p.getPid());
                it.remove();
            } else {
                break;
            }
        }
    }

    private void dispatchIfNeeded(int tick) {
        if (currentProcess == null && !readyQueue.isEmpty()) {
            currentProcess = strategy.selectNextProcess(readyQueue);
            quantumLeft = strategy.getQuantum();

            currentProcess.markFirstRun(tick);
            System.out.println("[CPU] Tick " + tick + " -> Ejecutando: " + currentProcess.getPid());
        }
    }

    public boolean isAllDone() {
        return pendingArrivals.isEmpty() && readyQueue.isEmpty() && currentProcess == null;
    }

    public void printReport() {
        System.out.println("\n================= REPORT =================");
        allProcesses.sort(Comparator.comparing(Process::getPid));

        double sumWait = 0;
        double sumTurn = 0;
        double sumResp = 0;

        for (Process p : allProcesses) {
            if (p.getFinishTick() == null) {
                System.out.println(p.getPid() + " -> NO terminó (finishTick=null)");
                continue;
            }

            int turnaround = p.getFinishTick() - p.getArrivalTime() + 1;
            int waiting = turnaround - p.getBurstTime();
            int response = (p.getFirstRunTick() == null) ? -1 : (p.getFirstRunTick() - p.getArrivalTime());

            sumWait += waiting;
            sumTurn += turnaround;
            sumResp += response;

            System.out.println(
                p.getPid()
                + " | arrival=" + p.getArrivalTime()
                + " burst=" + p.getBurstTime()
                + " firstRun=" + p.getFirstRunTick()
                + " finish=" + p.getFinishTick()
                + " | response=" + response
                + " waiting=" + waiting
                + " turnaround=" + turnaround
            );
        }

        int n = allProcesses.size();
        double avgWait = (n == 0) ? 0 : (sumWait / n);
        double avgTurn = (n == 0) ? 0 : (sumTurn / n);
        double avgResp = (n == 0) ? 0 : (sumResp / n);

        double utilization = (totalTicks == 0) ? 0 : (100.0 * busyTicks / totalTicks);

        System.out.println("------------------------------------------");
        System.out.println("Total ticks: " + totalTicks + " | Busy ticks: " + busyTicks);
        System.out.printf("CPU Utilization: %.2f%%\n", utilization);
        System.out.printf("Avg Waiting: %.2f | Avg Turnaround: %.2f | Avg Response: %.2f\n",
                avgWait, avgTurn, avgResp);
        System.out.println("==========================================\n");
    }

    @Override
    public void onTick(int tick) {

        totalTicks++;

        // 1) Llegadas
        enqueueArrivals(tick);

        // 2) Elegir proceso si no hay uno corriendo
        dispatchIfNeeded(tick);

        // 3) IDLE
        if (currentProcess == null) {
            System.out.println("[CPU] Tick " + tick + " -> IDLE (sin procesos)");
            return;
        }

        // 4) Ejecutar 1 tick
        busyTicks++;
        currentProcess.consumeOneTick();
        if (quantumLeft > 0) quantumLeft--;

        System.out.println("    [Process " + currentProcess.getPid() + "] tiempo restante: " + currentProcess.getRemainingTime());
        System.out.println("[CPU] Tick " + tick + " -> " + currentProcess.getPid()
                + " restante=" + currentProcess.getRemainingTime()
                + " quantumLeft=" + quantumLeft);

        // 5) Si terminó
        if (currentProcess.isFinished()) {
            currentProcess.markFinish(tick);
            strategy.onProcessFinished(currentProcess);
            System.out.println("[CPU] " + currentProcess.getPid() + " terminó.");

            currentProcess = null;
            quantumLeft = 0;
            return;
        }

        // 6) Si se acabó el quantum (solo si el algoritmo usa quantum)
        if (strategy.getQuantum() > 0 && quantumLeft == 0) {
            System.out.println("[CPU] Quantum terminado para " + currentProcess.getPid() + " -> reencolando");
            strategy.onQuantumExpired(currentProcess, readyQueue);
            currentProcess = null;
            quantumLeft = 0;
        }
    }
}
