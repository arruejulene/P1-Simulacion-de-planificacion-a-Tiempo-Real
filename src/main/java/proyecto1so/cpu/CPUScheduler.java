/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto1so.cpu;

/**
 *
 * @author ani
 */





import java.util.concurrent.Semaphore;
import proyecto1so.clock.ClockListener;
import proyecto1so.datastructures.Compare;
import proyecto1so.datastructures.OrderedQueue;
import proyecto1so.datastructures.Queue;
import proyecto1so.datastructures.SingleLinkedList;
import proyecto1so.model.Process;
import proyecto1so.model.ProcessState;
import proyecto1so.scheduler.PriorityPreemptiveStrategy;
import proyecto1so.scheduler.RoundRobinStrategy;
import proyecto1so.scheduler.SRTStrategy;
import proyecto1so.scheduler.SchedulerStrategy;

public class CPUScheduler implements ClockListener {

    private final Semaphore mutex = new Semaphore(1, true);

    // READY (cola propia)
    private final Queue<Process> readyQueue = new Queue<>();

    // Llegadas pendientes (ordenadas por arrivalTime)
    private final OrderedQueue<Process> pendingArrivals = new OrderedQueue<>(new Compare<Process>() {
        @Override
        public int compare(Process a, Process b) {
            return a.getArrivalTime() - b.getArrivalTime();
        }
    });

    // Terminados
    private final SingleLinkedList<Process> finished = new SingleLinkedList<>();

    // Estrategia actual
    private SchedulerStrategy strategy = new RoundRobinStrategy(2);

    // Proceso actual
    private Process currentProcess = null;
    private int quantumLeft = 0;

    // Métricas
    private int totalTicks = 0;
    private int busyTicks = 0;

    public void setStrategy(SchedulerStrategy strategy) {
        if (strategy != null) {
            this.strategy = strategy;
        }
    }

    public void addProcess(Process p) {
        if (p == null) return;

        // Si arrivalTime <= 0, le asignamos uno escalonado (como ya venías haciendo)
        Process toInsert = p;
        if (p.getArrivalTime() <= 0) {
            int arrival = 1 + (pendingArrivals.size() * 2);
            // OJO: si tu Process tiene priority, aquí conviene preservarlo:
            // Si tu constructor (pid, burst, arrival, priority) existe, usa ese.
            // Si no, deja el de 3 args.
            try {
                // intenta constructor con priority (si existe)
                toInsert = new Process(p.getPid(), p.getBurstTime(), arrival, p.getPriority());
            } catch (Throwable t) {
                // fallback si no existe el constructor de 4 args
                toInsert = new Process(p.getPid(), p.getBurstTime(), arrival);
            }
        }

        pendingArrivals.insertOrdered(toInsert);
    }

    @Override
    public void onTick(int tick) {
        try {
            mutex.acquire();
            totalTicks = tick;

            // 1) Llegadas -> READY
            while (!pendingArrivals.isEmpty() && pendingArrivals.peek().getArrivalTime() <= tick) {
                Process arriving = pendingArrivals.dequeue();
                arriving.setState(ProcessState.READY);
                readyQueue.enqueue(arriving);
                System.out.println("[CPU] Tick " + tick + " -> Llega: " + arriving.getPid());
            }

            // 2) PREEMPCIÓN (si aplica) ANTES de ejecutar el tick
            //    - PriorityPreemptive: si existe uno en READY con prioridad menor que el current
            //    - SRT: si existe uno en READY con remaining menor que el current
            if (currentProcess != null && shouldPreempt(currentProcess)) {
                System.out.println("[CPU] PREEMPT -> Sale: " + currentProcess.getPid() + " (vuelve a READY)");
                currentProcess.setState(ProcessState.READY);
                readyQueue.enqueue(currentProcess);
                currentProcess = null; // para forzar selección del nuevo
            }

            // 3) Si no hay proceso corriendo, seleccionar siguiente
            if (currentProcess == null) {
                currentProcess = strategy.selectNextProcess(readyQueue);

                if (currentProcess != null) {
                    currentProcess.setState(ProcessState.RUNNING);
                    currentProcess.markFirstRun(tick);
                    quantumLeft = strategy.getQuantum();

                    System.out.println("[CPU] Tick " + tick + " -> Ejecutando: " + currentProcess.getPid());
                } else {
                    // No hay ready
                    if (pendingArrivals.isEmpty()) {
                        System.out.println("[CPU] Tick " + tick + " -> IDLE (sin procesos)");
                    }
                    return;
                }
            }

            // 4) Ejecutar 1 tick de CPU
            busyTicks++;
            currentProcess.consumeOneTick();
            System.out.println("    [Process " + currentProcess.getPid() + "] tiempo restante: " + currentProcess.getRemainingTime());

            // 5) Quantum (si no es infinito)
            if (quantumLeft != Integer.MAX_VALUE) {
                quantumLeft--;
            }

            System.out.println("[CPU] Tick " + tick + " -> " + currentProcess.getPid()
                    + " restante=" + currentProcess.getRemainingTime()
                    + " quantumLeft=" + (quantumLeft == Integer.MAX_VALUE ? "INF" : quantumLeft));

            // 6) Finalización / Expiración de quantum
            if (currentProcess.isFinished()) {
                currentProcess.markFinish(tick);
                currentProcess.setState(ProcessState.TERMINATED);
                System.out.println("[CPU] " + currentProcess.getPid() + " terminó.");
                finished.addLast(currentProcess);
                currentProcess = null;
            } else if (quantumLeft == 0) {
                // Round Robin: reencolar
                currentProcess.setState(ProcessState.READY);
                readyQueue.enqueue(currentProcess);
                System.out.println("[CPU] Quantum terminado para " + currentProcess.getPid() + " -> reencolando");
                currentProcess = null;
            }

        } catch (InterruptedException e) {
            // ignore
        } finally {
            mutex.release();
        }
    }

    // ---------------- PREEMPCIÓN ----------------

    private boolean shouldPreempt(Process current) {
        if (readyQueue.isEmpty()) return false;

        // Priority preemptive: menor número = más prioridad
        if (strategy instanceof PriorityPreemptiveStrategy) {
            Process best = peekBestByPriority();
            if (best == null) return false;
            return isPriorityBetter(best, current);
        }

        // SRT: menor remainingTime preempta
        if (strategy instanceof SRTStrategy) {
            Process best = peekBestByRemainingTime();
            if (best == null) return false;
            return isRemainingBetter(best, current);
        }

        // FCFS / RR no preemptan por llegada
        return false;
    }

    // Mira el mejor candidato por PRIORIDAD sin alterar el contenido final de la cola
    private Process peekBestByPriority() {
        SingleLinkedList<Process> temp = new SingleLinkedList<>();
        Process best = null;

        while (!readyQueue.isEmpty()) {
            Process p = readyQueue.dequeue();
            temp.addLast(p);

            if (best == null || isPriorityBetter(p, best)) {
                best = p;
            }
        }

        while (!temp.isEmpty()) {
            readyQueue.enqueue(temp.removeFirst());
        }

        return best;
    }

    private boolean isPriorityBetter(Process a, Process b) {
        int diff = a.getPriority() - b.getPriority();
        if (diff != 0) return diff < 0; // menor priority = mejor
        return a.getPid().compareTo(b.getPid()) < 0;
    }

    // Mira el mejor candidato por REMAINING TIME sin alterar el contenido final de la cola
    private Process peekBestByRemainingTime() {
        SingleLinkedList<Process> temp = new SingleLinkedList<>();
        Process best = null;

        while (!readyQueue.isEmpty()) {
            Process p = readyQueue.dequeue();
            temp.addLast(p);

            if (best == null || isRemainingBetter(p, best)) {
                best = p;
            }
        }

        while (!temp.isEmpty()) {
            readyQueue.enqueue(temp.removeFirst());
        }

        return best;
    }

    private boolean isRemainingBetter(Process a, Process b) {
        int diff = a.getRemainingTime() - b.getRemainingTime();
        if (diff != 0) return diff < 0;
        return a.getPid().compareTo(b.getPid()) < 0;
    }

    // ---------------- REPORT ----------------

    public void printReport() {
        System.out.println("\n================= REPORT =================");

        SingleLinkedList<Process> temp = new SingleLinkedList<>();

        int count = 0;
        double sumWaiting = 0;
        double sumTurnaround = 0;
        double sumResponse = 0;

        while (!finished.isEmpty()) {
            Process p = finished.removeFirst();
            temp.addLast(p);

            int arrival = p.getArrivalTime();
            int burst = p.getBurstTime();
            Integer firstRun = p.getFirstRunTick();
            Integer finish = p.getFinishTick();

            if (firstRun == null) firstRun = arrival;
            if (finish == null) finish = totalTicks;

            int turnaround = finish - arrival + 1;
            int response = firstRun - arrival;
            int waiting = turnaround - burst;

            sumTurnaround += turnaround;
            sumResponse += response;
            sumWaiting += waiting;
            count++;

            System.out.println(p.getPid()
                    + " | arrival=" + arrival
                    + " burst=" + burst
                    + " firstRun=" + firstRun
                    + " finish=" + finish
                    + " | response=" + response
                    + " waiting=" + waiting
                    + " turnaround=" + turnaround
                    + " | state=" + p.getState());
        }

        while (!temp.isEmpty()) {
            finished.addLast(temp.removeFirst());
        }

        System.out.println("------------------------------------------");
        System.out.println("Total ticks: " + totalTicks + " | Busy ticks: " + busyTicks);

        double util = (totalTicks == 0) ? 0.0 : ((double) busyTicks / (double) totalTicks) * 100.0;
        System.out.printf("CPU Utilization: %.2f%%%n", util);

        if (count > 0) {
            System.out.printf("Avg Waiting: %.2f | Avg Turnaround: %.2f | Avg Response: %.2f%n",
                    (sumWaiting / count), (sumTurnaround / count), (sumResponse / count));
        }

        System.out.println("==========================================\n");
    }
}
