package proyecto1so.cpu;

import java.util.concurrent.Semaphore;
import proyecto1so.clock.ClockListener;
import proyecto1so.datastructures.Compare;
import proyecto1so.datastructures.OrderedQueue;
import proyecto1so.datastructures.Queue;
import proyecto1so.datastructures.SingleLinkedList;
import proyecto1so.memory.SuspensionPolicy;
import proyecto1so.model.Process;
import proyecto1so.model.ProcessState;
import proyecto1so.scheduler.EDFStrategy;
import proyecto1so.scheduler.PriorityPreemptiveStrategy;
import proyecto1so.scheduler.RoundRobinStrategy;
import proyecto1so.scheduler.SRTStrategy;
import proyecto1so.scheduler.SchedulerStrategy;

/**
 * Scheduler de un CPU para una simulación de un "satelite".
 *
 * Que hace en resumen:
 * mantiene colas típicas del sistema operativo: READY, BLOCKED y sus SUSPENDED.
 * administra llegadas por tick mediante una cola ordenada.
 * ejecuta un proceso por tick según una estrategia (roundrobin, FCFS entre otras).
 * permite preempción.
 * simula interrupciones "externas".
 * simula eventos de I/O que bloquean al proceso y luego lo regresan a READY.
 * limita cuantos procesos pueden estar “en RAM” (simula swap in/out).
 *
 * tratado de la concurrencia
 * el reloj (onTick) y los triggers (I/O e interrupciones) pueden pasar “al mismo tiempo”.
 * se usa un mutex (semaforo) para que las colas/estado no queden inconsistentes.
 */
public class CPUScheduler implements ClockListener {

    // semaforo (llamado mutex) para proteger estado compartido
    private final Semaphore mutex = new Semaphore(1, true);

    // colas de procesos
    private final Queue<Process> readyQueue = new Queue<>();


    private final Queue<Process> blockedQueue = new Queue<>();

  
    private final Queue<Process> readySuspendedQueue = new Queue<>();

    private final Queue<Process> blockedSuspendedQueue = new Queue<>();

    //procesos que llegan con un arrival time.
    
    private final OrderedQueue<Process> pendingArrivals =
            new OrderedQueue<>(new Compare<Process>() {
                @Override
                public int compare(Process a, Process b) {
                    return a.getArrivalTime() - b.getArrivalTime();
                }
            });

    //listas de procesos que ya terminaron
    private final SingleLinkedList<Process> finished = new SingleLinkedList<>();

    //estrategia de schedule
    private SchedulerStrategy strategy = new RoundRobinStrategy(2);

    //proceso que esta running
    private Process currentProcess = null;

    private int quantumLeft = 0;

    // limite de procesos que pueden estar en RAM a la vez
    private final int maxInRam;

    //politica para swapout
    private final SuspensionPolicy suspensionPolicy;

 
    //para metricas de la RAM
    private int inRamCount = 0;


    private int totalSuspensions = 0;


    private int totalSwapIns = 0;


    private int totalTicks = 0;

    private int busyTicks = 0;

    // Para metricas de deadlines
    private int deadlinesMet = 0;
    private int deadlinesMissed = 0;

    // Ahora la parte de interrupciones
    private boolean interruptInProgress = false;


    private int interruptTicksLeft = 0;


    private String interruptName = null;


    private int totalInterrupts = 0;


    private int interruptPreemptions = 0;


    private int isrBusyTicks = 0;

    private int tickDurationMs = 300;


    private int ioEventsRequested = 0;


    private int ioEventsCompleted = 0;

    
    public CPUScheduler() {
        this(Integer.MAX_VALUE, SuspensionPolicy.LOWEST_PRIORITY);
    }

    //constructor principal 
   
    public CPUScheduler(int maxInRam, SuspensionPolicy suspensionPolicy) {
        this.maxInRam = maxInRam <= 0 ? Integer.MAX_VALUE : maxInRam;
        this.suspensionPolicy = suspensionPolicy == null
                ? SuspensionPolicy.LOWEST_PRIORITY
                : suspensionPolicy;
    }

    //para cambiar la estrategia de schedule
    public void setStrategy(SchedulerStrategy strategy) {
        if (strategy != null) this.strategy = strategy;
    }

    //cuantos milisegundas dura el tick
    public void setTickDurationMs(int tickDurationMs) {
        if (tickDurationMs > 0) this.tickDurationMs = tickDurationMs;
    }

    //para agregar processos a la simulacion
    public void addProcess(Process p) {
        if (p == null) return;

        if (p.getArrivalTime() <= 0) {
            int arrival = 1 + (pendingArrivals.size() * 2);
            p = new Process(
                    p.getPid(),
                    p.getBurstTime(),
                    arrival,
                    p.getPriority(),
                    p.getDeadlineTick()
            );
        }

        pendingArrivals.insertOrdered(p);
    }

    // aqui es donde se generan interrupciones "externas" 
    public boolean triggerExternalInterrupt(String name, int isrTicks) {
        if (isrTicks <= 0) return false;
        String resolvedName = (name == null || name.isBlank()) ? "EXT-IRQ" : name;

        boolean acquired = false;
        try {
            mutex.acquire();
            acquired = true;

            totalInterrupts++;
            interruptInProgress = true;
            interruptTicksLeft += isrTicks;
            interruptName = resolvedName;

            if (currentProcess != null) {
                currentProcess.setState(ProcessState.READY);
                readyQueue.enqueue(currentProcess);
                System.out.println("[INT] Preemptando " + currentProcess.getPid()
                        + " por " + interruptName + " (ISR=" + isrTicks + " ticks)");
                currentProcess = null;
                interruptPreemptions++;
            } else {
                System.out.println("[INT] Interrupción " + interruptName
                        + " registrada (ISR=" + isrTicks + " ticks)");
            }
            return true;
        } catch (InterruptedException e) {
            return false;
        } finally {
            if (acquired) mutex.release();
        }
    }

    //simulación de interrupciones internas, despues se llama para que ocurra de manera aleatoria
    public boolean triggerIOEvent(String reason, int serviceTicks) {
        if (serviceTicks <= 0) return false;
        String resolvedReason = (reason == null || reason.isBlank()) ? "IO" : reason;

        boolean acquired = false;
        try {
            mutex.acquire();
            acquired = true;

            if (currentProcess == null) {
                System.out.println("[IO] Evento " + resolvedReason + " ignorado: CPU sin proceso RUNNING");
                return false;
            }

            Process blocked = currentProcess;
            blocked.setState(ProcessState.BLOCKED);
            blockedQueue.enqueue(blocked);
            currentProcess = null;
            ioEventsRequested++;

            System.out.println("[IO] " + blocked.getPid() + " -> BLOCKED por " + resolvedReason
                    + " (serviceTicks=" + serviceTicks + ")");

            IOCompletionWorker worker = new IOCompletionWorker(blocked, resolvedReason, serviceTicks);
            worker.start();
            return true;
        } catch (InterruptedException e) {
            return false;
        } finally {
            if (acquired) mutex.release();
        }
    }

    //parte de snapshot, se usa para poder ver todo en la interfaz
    public Process getCurrentProcessSnapshot() {
        boolean acquired = false;
        try {
            mutex.acquire();
            acquired = true;
            return currentProcess;
        } catch (InterruptedException e) {
            return null;
        } finally {
            if (acquired) mutex.release();
        }
    }

   
    public Process[] snapshotReadyQueue() {
        return snapshotQueue(readyQueue);
    }

  
    public Process[] snapshotBlockedQueue() {
        return snapshotQueue(blockedQueue);
    }

    
    public Process[] snapshotReadySuspendedQueue() {
        return snapshotQueue(readySuspendedQueue);
    }

    public Process[] snapshotBlockedSuspendedQueue() {
        return snapshotQueue(blockedSuspendedQueue);
    }

    public Process[] snapshotFinishedQueue() {
        boolean acquired = false;
        try {
            mutex.acquire();
            acquired = true;

            Process[] values = new Process[finished.size()];
            int i = 0;

            SingleLinkedList<Process> temp = new SingleLinkedList<>();
            while (!finished.isEmpty()) {
                Process p = finished.removeFirst();
                temp.addLast(p);
                values[i++] = p;
            }
            while (!temp.isEmpty()) finished.addLast(temp.removeFirst());
            return values;

        } catch (InterruptedException e) {
            return new Process[0];
        } finally {
            if (acquired) mutex.release();
        }
    }

    public int getTotalTicksSnapshot() {
        boolean acquired = false;
        try {
            mutex.acquire();
            acquired = true;
            return totalTicks;
        } catch (InterruptedException e) {
            return totalTicks;
        } finally {
            if (acquired) mutex.release();
        }
    }

    public int getBusyTicksSnapshot() {
        boolean acquired = false;
        try {
            mutex.acquire();
            acquired = true;
            return busyTicks;
        } catch (InterruptedException e) {
            return busyTicks;
        } finally {
            if (acquired) mutex.release();
        }
    }

    public int getDeadlinesMetSnapshot() {
        boolean acquired = false;
        try {
            mutex.acquire();
            acquired = true;
            return deadlinesMet;
        } catch (InterruptedException e) {
            return deadlinesMet;
        } finally {
            if (acquired) mutex.release();
        }
    }

    public int getDeadlinesMissedSnapshot() {
        boolean acquired = false;
        try {
            mutex.acquire();
            acquired = true;
            return deadlinesMissed;
        } catch (InterruptedException e) {
            return deadlinesMissed;
        } finally {
            if (acquired) mutex.release();
        }
    }

    public String getStrategyNameSnapshot() {
        boolean acquired = false;
        try {
            mutex.acquire();
            acquired = true;
            return strategy == null ? "-" : strategy.getClass().getSimpleName();
        } catch (InterruptedException e) {
            return "-";
        } finally {
            if (acquired) mutex.release();
        }
    }

    public int getInRamCountSnapshot() {
        boolean acquired = false;
        try {
            mutex.acquire();
            acquired = true;
            return inRamCount;
        } catch (InterruptedException e) {
            return inRamCount;
        } finally {
            if (acquired) mutex.release();
        }
    }

    public int getMaxInRamSnapshot() {
        return maxInRam;
    }

    /**
     * Importante aca esta el tick principal de la simulación
     *
     * actualiza el totalclicks
     * Mete a READY/READY_SUSPENDED los procesos que ya llegan.
     * Hace swap-in si hay espacio en RAM y hay suspendidos esperando.
     * Si hay una interrupción (ISR), consume 1 tick de ISR y sale.
     * Si la estrategia es preemptiva, revisa si hay alguien “mejor” en READY que el RUNNING.
     * Si no hay proceso corriendo, pide a la estrategia el próximo.
     * Ejecuta 1 tick del proceso: consume CPU, maneja quantum y finalización.
     *
     * Concurrencia:
     * - Todo el método está bajo mutex.
     */
    @Override
    public void onTick(int tick) {
        boolean acquired = false;
        try {
            mutex.acquire();
            acquired = true;
            totalTicks = tick;

            while (!pendingArrivals.isEmpty() && pendingArrivals.peek().getArrivalTime() <= tick) {
                Process arriving = pendingArrivals.dequeue();
                admitArrivingProcess(arriving, tick);
            }

            while (swapInOneIfPossible()) {
                // Fill available RAM slots with suspended processes.
            }

            if (interruptInProgress) {
                busyTicks++;
                isrBusyTicks++;
                interruptTicksLeft--;

                System.out.println("[ISR] Tick " + tick + " -> Atendiendo " + interruptName
                        + " | restantes=" + interruptTicksLeft);

                if (interruptTicksLeft <= 0) {
                    interruptInProgress = false;
                    interruptName = null;
                    interruptTicksLeft = 0;
                    System.out.println("[ISR] Tick " + tick + " -> Fin ISR, retomando scheduler");
                }
                return;
            }

            if (currentProcess != null && shouldPreempt(currentProcess)) {
                System.out.println("[CPU] PREEMPT -> Sale: " + currentProcess.getPid() + " (vuelve a READY)");
                currentProcess.setState(ProcessState.READY);
                readyQueue.enqueue(currentProcess);
                currentProcess = null;
            }

            if (currentProcess == null) {
                currentProcess = strategy.selectNextProcess(readyQueue);

                if (currentProcess != null) {
                    currentProcess.setState(ProcessState.RUNNING);
                    currentProcess.markFirstRun(tick);
                    quantumLeft = strategy.getQuantum();
                    System.out.println("[CPU] Tick " + tick + " -> Ejecutando: " + currentProcess.getPid());
                } else {
                    System.out.println("[CPU] Tick " + tick + " -> IDLE (sin procesos READY en RAM)");
                    return;
                }
            }

            busyTicks++;
            currentProcess.consumeOneTick();
            System.out.println("    [Process " + currentProcess.getPid() + "] tiempo restante: " + currentProcess.getRemainingTime());

            if (quantumLeft != Integer.MAX_VALUE) quantumLeft--;

            System.out.println("[CPU] Tick " + tick + " -> " + currentProcess.getPid()
                    + " restante=" + currentProcess.getRemainingTime()
                    + " quantumLeft=" + (quantumLeft == Integer.MAX_VALUE ? "INF" : quantumLeft));

            if (currentProcess.isFinished()) {
                currentProcess.markFinish(tick);
                currentProcess.setState(ProcessState.TERMINATED);

                if (currentProcess.getDeadlineTick() != Integer.MAX_VALUE) {
                    if (tick <= currentProcess.getDeadlineTick()) deadlinesMet++;
                    else deadlinesMissed++;
                }

                System.out.println("[CPU] " + currentProcess.getPid() + " terminó.");
                finished.addLast(currentProcess);
                currentProcess = null;
                if (inRamCount > 0) inRamCount--;
                swapInOneIfPossible();

            } else if (quantumLeft == 0) {
                currentProcess.setState(ProcessState.READY);
                readyQueue.enqueue(currentProcess);
                System.out.println("[CPU] Quantum terminado para " + currentProcess.getPid() + " -> reencolando");
                currentProcess = null;
            }

        } catch (InterruptedException e) {
            // Keep scheduler alive.
        } finally {
            if (acquired) mutex.release();
        }
    }

  
    private void admitArrivingProcess(Process arriving, int tick) {
        if (arriving == null) return;

        if (inRamCount < maxInRam) {
            arriving.setState(ProcessState.READY);
            readyQueue.enqueue(arriving);
            inRamCount++;
            System.out.println("[CPU] Tick " + tick + " -> Llega: " + arriving.getPid()
                    + " (READY en RAM " + inRamCount + "/" + maxInRam + ")");
            return;
        }

        boolean freed = suspendOneVictim();
        if (freed && inRamCount < maxInRam) {
            arriving.setState(ProcessState.READY);
            readyQueue.enqueue(arriving);
            inRamCount++;
            System.out.println("[CPU] Tick " + tick + " -> Llega: " + arriving.getPid()
                    + " (READY en RAM tras suspensión " + inRamCount + "/" + maxInRam + ")");
            return;
        }

        arriving.setState(ProcessState.READY_SUSPENDED);
        readySuspendedQueue.enqueue(arriving);
        System.out.println("[CPU] Tick " + tick + " -> RAM llena, " + arriving.getPid()
                + " va a READY_SUSPENDED");
    }

    /**
     * Snapshot genérico de una cola FIFO sin romper el orden final.
     * Se usa para la UI/monitor de colas.
     */
    private Process[] snapshotQueue(Queue<Process> q) {
        boolean acquired = false;
        try {
            mutex.acquire();
            acquired = true;

            Process[] values = new Process[q.size()];
            int idx = 0;
            Queue<Process> temp = new Queue<>();

            int n = q.size();
            for (int i = 0; i < n; i++) {
                Process p = q.dequeue();
                if (p == null) break;
                values[idx++] = p;
                temp.enqueue(p);
            }

            int m = temp.size();
            for (int i = 0; i < m; i++) {
                Process p = temp.dequeue();
                if (p == null) break;
                q.enqueue(p);
            }

            if (idx == values.length) return values;

            Process[] trimmed = new Process[idx];
            for (int i = 0; i < idx; i++) trimmed[i] = values[i];
            return trimmed;

        } catch (InterruptedException e) {
            return new Process[0];
        } finally {
            if (acquired) mutex.release();
        }
    }

    private boolean suspendOneVictim() {
        Process victimReady = pickVictimFromQueue(readyQueue);
        if (victimReady != null) {
            victimReady.setState(ProcessState.READY_SUSPENDED);
            readySuspendedQueue.enqueue(victimReady);
            if (inRamCount > 0) inRamCount--;
            totalSuspensions++;
            System.out.println("[SWAP OUT] READY -> READY_SUSPENDED: " + victimReady.getPid()
                    + " | inRam=" + inRamCount + "/" + maxInRam);
            return true;
        }

        Process victimBlocked = pickVictimFromQueue(blockedQueue);
        if (victimBlocked != null) {
            victimBlocked.setState(ProcessState.BLOCKED_SUSPENDED);
            blockedSuspendedQueue.enqueue(victimBlocked);
            if (inRamCount > 0) inRamCount--;
            totalSuspensions++;
            System.out.println("[SWAP OUT] BLOCKED -> BLOCKED_SUSPENDED: " + victimBlocked.getPid()
                    + " | inRam=" + inRamCount + "/" + maxInRam);
            return true;
        }

        return false;
    }

    /**
     * Intenta traer (swap in) un proceso suspendido si hay cupo.
     *
     * Prioriza:
     * - READY_SUSPENDED -> READY
     * - luego BLOCKED_SUSPENDED -> BLOCKED
     */
    private boolean swapInOneIfPossible() {
        if (inRamCount >= maxInRam) return false;

        Process fromReadySusp = readySuspendedQueue.dequeue();
        if (fromReadySusp != null) {
            fromReadySusp.setState(ProcessState.READY);
            readyQueue.enqueue(fromReadySusp);
            inRamCount++;
            totalSwapIns++;
            System.out.println("[SWAP IN] READY_SUSPENDED -> READY: " + fromReadySusp.getPid()
                    + " | inRam=" + inRamCount + "/" + maxInRam);
            return true;
        }

        Process fromBlockedSusp = blockedSuspendedQueue.dequeue();
        if (fromBlockedSusp != null) {
            fromBlockedSusp.setState(ProcessState.BLOCKED);
            blockedQueue.enqueue(fromBlockedSusp);
            inRamCount++;
            totalSwapIns++;
            System.out.println("[SWAP IN] BLOCKED_SUSPENDED -> BLOCKED: " + fromBlockedSusp.getPid()
                    + " | inRam=" + inRamCount + "/" + maxInRam);
            return true;
        }

        return false;
    }

    
    private Process pickVictimFromQueue(Queue<Process> q) {
        if (q == null || q.isEmpty()) return null;

        Queue<Process> temp = new Queue<>();
        Process victim = null;

        int n = q.size();
        for (int i = 0; i < n; i++) {
            Process p = q.dequeue();
            if (p == null) break;

            if (victim == null) victim = p;
            else if (isWorseForRam(p, victim)) victim = p;

            temp.enqueue(p);
        }

        boolean removed = false;
        int m = temp.size();
        for (int i = 0; i < m; i++) {
            Process p = temp.dequeue();
            if (p == null) break;
            if (!removed && p == victim) {
                removed = true;
                continue;
            }
            q.enqueue(p);
        }

        return victim;
    }

  
    private boolean isWorseForRam(Process a, Process b) {
        if (a == null) return false;
        if (b == null) return true;

        switch (suspensionPolicy) {
            case FARTHEST_DEADLINE:
                if (a.getDeadlineTick() != b.getDeadlineTick()) {
                    return a.getDeadlineTick() > b.getDeadlineTick();
                }
                return a.getPriority() > b.getPriority();

            case LOWEST_PRIORITY:
            default:
                if (a.getPriority() != b.getPriority()) {
                    return a.getPriority() > b.getPriority();
                }
                return a.getDeadlineTick() > b.getDeadlineTick();
        }
    }

  
    private boolean shouldPreempt(Process current) {
        if (readyQueue.isEmpty()) return false;

        if (strategy instanceof PriorityPreemptiveStrategy) {
            Process best = peekBestByPriority();
            return best != null && isPriorityBetter(best, current);
        }

        if (strategy instanceof SRTStrategy) {
            Process best = peekBestByRemaining();
            return best != null && isRemainingBetter(best, current);
        }

        if (strategy instanceof EDFStrategy) {
            Process best = peekBestByDeadline();
            return best != null && isDeadlineBetter(best, current);
        }

        return false;
    }

    /**
     * Busca el “mejor” proceso por prioridad dentro de READY sin cambiar el orden final.
     */
    private Process peekBestByPriority() {
        SingleLinkedList<Process> temp = new SingleLinkedList<>();
        Process best = null;

        while (!readyQueue.isEmpty()) {
            Process p = readyQueue.dequeue();
            temp.addLast(p);
            if (best == null || isPriorityBetter(p, best)) best = p;
        }

        while (!temp.isEmpty()) readyQueue.enqueue(temp.removeFirst());
        return best;
    }


    private boolean isPriorityBetter(Process a, Process b) {
        int diff = a.getPriority() - b.getPriority();
        if (diff != 0) return diff < 0;
        return a.getPid().compareTo(b.getPid()) < 0;
    }


    private Process peekBestByRemaining() {
        SingleLinkedList<Process> temp = new SingleLinkedList<>();
        Process best = null;

        while (!readyQueue.isEmpty()) {
            Process p = readyQueue.dequeue();
            temp.addLast(p);
            if (best == null || isRemainingBetter(p, best)) best = p;
        }

        while (!temp.isEmpty()) readyQueue.enqueue(temp.removeFirst());
        return best;
    }


    private boolean isRemainingBetter(Process a, Process b) {
        int diff = a.getRemainingTime() - b.getRemainingTime();
        if (diff != 0) return diff < 0;
        return a.getPid().compareTo(b.getPid()) < 0;
    }


    private Process peekBestByDeadline() {
        SingleLinkedList<Process> temp = new SingleLinkedList<>();
        Process best = null;

        while (!readyQueue.isEmpty()) {
            Process p = readyQueue.dequeue();
            temp.addLast(p);
            if (best == null || isDeadlineBetter(p, best)) best = p;
        }

        while (!temp.isEmpty()) readyQueue.enqueue(temp.removeFirst());
        return best;
    }


    private boolean isDeadlineBetter(Process a, Process b) {
        int da = a.getDeadlineTick();
        int db = b.getDeadlineTick();
        if (da != db) return da < db;
        return a.getPid().compareTo(b.getPid()) < 0;
    }

   //reporte de las metricas en terminal 
    
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
                    + " | state=" + p.getState()
                    + " | deadline=" + (p.getDeadlineTick() == Integer.MAX_VALUE ? "-" : p.getDeadlineTick()));
        }

        while (!temp.isEmpty()) finished.addLast(temp.removeFirst());

        System.out.println("------------------------------------------");
        System.out.println("Total ticks: " + totalTicks + " | Busy ticks: " + busyTicks);

        double util = (totalTicks == 0) ? 0.0 : ((double) busyTicks / (double) totalTicks) * 100.0;
        System.out.printf("CPU Utilization: %.2f%%%n", util);

        double throughput = (totalTicks == 0) ? 0.0 : ((double) count / (double) totalTicks);
        System.out.printf("Throughput: %.4f procesos/tick (%d procesos en %d ticks)%n",
                throughput, count, totalTicks);

        if (count > 0) {
            System.out.printf("Avg Waiting: %.2f | Avg Turnaround: %.2f | Avg Response: %.2f%n",
                    (sumWaiting / count), (sumTurnaround / count), (sumResponse / count));
        }

        int totalWithDeadline = deadlinesMet + deadlinesMissed;
        if (totalWithDeadline > 0) {
            double rate = (100.0 * deadlinesMet) / totalWithDeadline;
            System.out.printf("Deadlines met: %d | missed: %d | success rate: %.2f%%%n",
                    deadlinesMet, deadlinesMissed, rate);
        }

        System.out.println("Interrupts: total=" + totalInterrupts
                + " | preemptions=" + interruptPreemptions
                + " | ISR busy ticks=" + isrBusyTicks);

        System.out.println("I/O events: requested=" + ioEventsRequested
                + " | completed=" + ioEventsCompleted
                + " | blockedQueueSize=" + blockedQueue.size());

        System.out.println("RAM: inRam=" + inRamCount + "/" + maxInRam
                + " | suspensions=" + totalSuspensions
                + " | swapIns=" + totalSwapIns
                + " | readySuspended=" + readySuspendedQueue.size()
                + " | blockedSuspended=" + blockedSuspendedQueue.size());

        System.out.println("==========================================\n");
    }

  
    private void completeIO(Process process, String reason, int serviceTicks) {
        boolean acquired = false;
        try {
            mutex.acquire();
            acquired = true;

            if (removeOneFromQueue(blockedQueue, process)) {
                process.setState(ProcessState.READY);
                readyQueue.enqueue(process);
                ioEventsCompleted++;
                System.out.println("[IO] " + process.getPid() + " BLOCKED -> READY"
                        + " tras " + reason + " (serviceTicks=" + serviceTicks + ")");
                return;
            }

            if (removeOneFromQueue(blockedSuspendedQueue, process)) {
                process.setState(ProcessState.READY_SUSPENDED);
                readySuspendedQueue.enqueue(process);
                ioEventsCompleted++;
                System.out.println("[IO] " + process.getPid() + " BLOCKED_SUSPENDED -> READY_SUSPENDED"
                        + " tras " + reason + " (serviceTicks=" + serviceTicks + ")");
                swapInOneIfPossible();
            }

        } catch (InterruptedException e) {
            // Keep simulation running.
        } finally {
            if (acquired) mutex.release();
        }
    }

    private boolean removeOneFromQueue(Queue<Process> q, Process target) {
        if (q == null || q.isEmpty() || target == null) return false;

        SingleLinkedList<Process> temp = new SingleLinkedList<>();
        boolean removed = false;

        int n = q.size();
        for (int i = 0; i < n; i++) {
            Process p = q.dequeue();
            if (p == null) break;

            if (!removed && p == target) {
                removed = true;
                continue;
            }
            temp.addLast(p);
        }

        while (!temp.isEmpty()) q.enqueue(temp.removeFirst());
        return removed;
    }


    private class IOCompletionWorker extends Thread {
        private final Process process;
        private final String reason;
        private final int serviceTicks;

        IOCompletionWorker(Process process, String reason, int serviceTicks) {
            this.process = process;
            this.reason = reason;
            this.serviceTicks = serviceTicks;
            setName("IOCompletion-" + process.getPid());
        }

        @Override
        public void run() {
            try {
                Thread.sleep((long) serviceTicks * tickDurationMs);
            } catch (InterruptedException e) {
                // Complete immediately when interrupted.
            }
            completeIO(process, reason, serviceTicks);
        }
    }
}