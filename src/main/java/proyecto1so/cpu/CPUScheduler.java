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
import proyecto1so.datastructures.Compare;
import proyecto1so.datastructures.OrderedQueue;
import proyecto1so.datastructures.Queue;
import proyecto1so.model.Process;
import proyecto1so.model.ProcessState;
import proyecto1so.scheduler.RoundRobinStrategy;
import proyecto1so.scheduler.SchedulerStrategy;

public class CPUScheduler implements ClockListener {

    private final Queue<Process> readyQueue = new Queue<>();

    // Llegadas pendientes (ordenadas por arrivalTime) usando OrderedQueue propia
    private final OrderedQueue<Process> pendingArrivals = new OrderedQueue<>(
        new Compare<Process>() {
            @Override
            public int compare(Process a, Process b) {
                return a.getArrivalTime() - b.getArrivalTime();
            }
        }
    );

    // Para reporte final (guardamos todos los procesos en una cola propia y rotamos)
    private final Queue<Process> allProcesses = new Queue<>();
    private int allCount = 0;

    private Process currentProcess = null;
    private int quantumLeft = 0;

    private int totalTicks = 0;
    private int busyTicks = 0;

    // Strategy actual (por defecto RR quantum=2)
    private SchedulerStrategy strategy = new RoundRobinStrategy(2);

    public void setStrategy(SchedulerStrategy strategy) {
        if (strategy == null) return;
        this.strategy = strategy;

        // Si hay proceso corriendo, reseteamos quantum según nuevo algoritmo
        if (currentProcess != null) {
            quantumLeft = this.strategy.getQuantum();
        }
    }

    public SchedulerStrategy getStrategy() {
        return strategy;
    }

    public void addProcess(Process p) {
        // Asegurar estado inicial (por si lo crearon distinto)
        p.setState(ProcessState.NEW);

        // llega a la estructura ordenada por arrival
        pendingArrivals.insertOrdered(p);

        // para el reporte
        allProcesses.enqueue(p);
        allCount++;
    }

    private void enqueueArrivals(int tick) {
        // mientras el primero en pendingArrivals ya debería haber llegado, pásalo a READY
        while (!pendingArrivals.isEmpty()) {
            Process next = pendingArrivals.peek();
            if (next == null) break;

            if (next.getArrivalTime() <= tick) {
                Process arrived = pendingArrivals.dequeue();

                // ✅ NEW -> READY
                arrived.setState(ProcessState.READY);

                readyQueue.enqueue(arrived);
                System.out.println("[CPU] Tick " + tick + " -> Llega: " + arrived.getPid());
            } else {
                break;
            }
        }
    }

    private void dispatchIfNeeded(int tick) {
        if (currentProcess != null) return;

        currentProcess = strategy.selectNextProcess(readyQueue);
        if (currentProcess == null) return;

        // ✅ READY -> RUNNING
        currentProcess.setState(ProcessState.RUNNING);

        quantumLeft = strategy.getQuantum();
        currentProcess.markFirstRun(tick);

        System.out.println("[CPU] Tick " + tick + " -> Ejecutando: " + currentProcess.getPid());
    }

    public boolean isAllDone() {
        return pendingArrivals.isEmpty() && currentProcess == null && readyQueue.peek() == null;
    }

    @Override
    public void onTick(int tick) {
        totalTicks++;

        // 1) Llegadas
        enqueueArrivals(tick);

        // 2) Dispatch si no hay proceso corriendo
        dispatchIfNeeded(tick);

        // 3) IDLE
        if (currentProcess == null) {
            System.out.println("[CPU] Tick " + tick + " -> IDLE (sin procesos)");
            return;
        }

        // 4) Ejecutar 1 tick
        busyTicks++;
        currentProcess.consumeOneTick();

        // quantum solo si aplica
        if (quantumLeft > 0) quantumLeft--;

        System.out.println("    [Process " + currentProcess.getPid() + "] tiempo restante: " + currentProcess.getRemainingTime());
        System.out.println("[CPU] Tick " + tick + " -> " + currentProcess.getPid()
                + " restante=" + currentProcess.getRemainingTime()
                + " quantumLeft=" + quantumLeft);

        // 5) Si terminó
        if (currentProcess.isFinished()) {
            currentProcess.markFinish(tick);

            // ✅ RUNNING -> TERMINATED
            currentProcess.setState(ProcessState.TERMINATED);

            strategy.onProcessFinished(currentProcess);
            System.out.println("[CPU] " + currentProcess.getPid() + " terminó.");

            currentProcess = null;
            quantumLeft = 0;
            return;
        }

        // 6) Si se acabó el quantum (solo si el algoritmo usa quantum)
        if (strategy.getQuantum() > 0 && quantumLeft == 0) {
            // Si se reencola, vuelve a READY
            currentProcess.setState(ProcessState.READY);

            System.out.println("[CPU] Quantum terminado para " + currentProcess.getPid() + " -> reencolando");
            strategy.onQuantumExpired(currentProcess, readyQueue);

            currentProcess = null;
            quantumLeft = 0;
        }
    }

    public void printReport() {
        System.out.println("\n================= REPORT =================");

        double sumWait = 0;
        double sumTurn = 0;
        double sumResp = 0;

        int finishedCount = 0;

        // Rotamos la cola allProcesses para poder recorrerla sin perder datos
        for (int i = 0; i < allCount; i++) {
            Process p = allProcesses.dequeue();
            allProcesses.enqueue(p);

            Integer finish = p.getFinishTick();
            Integer firstRun = p.getFirstRunTick();

            if (finish == null || firstRun == null) {
                System.out.println(p.getPid() + " -> NO terminó o no corrió (finish/firstRun null)");
                continue;
            }

            int turnaround = finish - p.getArrivalTime() + 1;
            int waiting = turnaround - p.getBurstTime();
            int response = firstRun - p.getArrivalTime();

            sumWait += waiting;
            sumTurn += turnaround;
            sumResp += response;
            finishedCount++;

            System.out.println(
                p.getPid()
                + " | arrival=" + p.getArrivalTime()
                + " burst=" + p.getBurstTime()
                + " firstRun=" + firstRun
                + " finish=" + finish
                + " | response=" + response
                + " waiting=" + waiting
                + " turnaround=" + turnaround
                + " | state=" + p.getState()
            );
        }

        double utilization = (totalTicks == 0) ? 0 : (100.0 * busyTicks / totalTicks);

        double avgWait = (finishedCount == 0) ? 0 : (sumWait / finishedCount);
        double avgTurn = (finishedCount == 0) ? 0 : (sumTurn / finishedCount);
        double avgResp = (finishedCount == 0) ? 0 : (sumResp / finishedCount);

        System.out.println("------------------------------------------");
        System.out.println("Total ticks: " + totalTicks + " | Busy ticks: " + busyTicks);
        System.out.printf("CPU Utilization: %.2f%%\n", utilization);
        System.out.printf("Avg Waiting: %.2f | Avg Turnaround: %.2f | Avg Response: %.2f\n",
                avgWait, avgTurn, avgResp);
        System.out.println("==========================================\n");
    }
}
