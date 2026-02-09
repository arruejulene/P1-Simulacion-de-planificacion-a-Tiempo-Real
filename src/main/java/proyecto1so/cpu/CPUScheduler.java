package proyecto1so.cpu;




import java.util.concurrent.Semaphore;
import proyecto1so.clock.ClockListener;
import proyecto1so.datastructures.Compare;
import proyecto1so.datastructures.OrderedQueue;
import proyecto1so.datastructures.Queue;
import proyecto1so.datastructures.SingleLinkedList;
import proyecto1so.model.Process;
import proyecto1so.model.ProcessState;
import proyecto1so.scheduler.EDFStrategy;
import proyecto1so.scheduler.PriorityPreemptiveStrategy;
import proyecto1so.scheduler.RoundRobinStrategy;
import proyecto1so.scheduler.SRTStrategy;
import proyecto1so.scheduler.SchedulerStrategy;

public class CPUScheduler implements ClockListener {

    private final Semaphore mutex = new Semaphore(1, true);

    private final Queue<Process> readyQueue = new Queue<>();

    private final OrderedQueue<Process> pendingArrivals =
            new OrderedQueue<>(new Compare<Process>() {
                @Override
                public int compare(Process a, Process b) {
                    return a.getArrivalTime() - b.getArrivalTime();
                }
            });

    private final SingleLinkedList<Process> finished = new SingleLinkedList<>();

    private SchedulerStrategy strategy = new RoundRobinStrategy(2);

    private Process currentProcess = null;
    private int quantumLeft = 0;

    private int totalTicks = 0;
    private int busyTicks = 0;

    // EDF metrics
    private int deadlinesMet = 0;
    private int deadlinesMissed = 0;

    // Interrupt handling metrics/state
    private boolean interruptInProgress = false;
    private int interruptTicksLeft = 0;
    private String interruptName = null;
    private int totalInterrupts = 0;
    private int interruptPreemptions = 0;
    private int isrBusyTicks = 0;

    public void setStrategy(SchedulerStrategy strategy) {
        if (strategy != null) this.strategy = strategy;
    }

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

    @Override
    public void onTick(int tick) {
        boolean acquired = false;
        try {
            mutex.acquire();
            acquired = true;
            totalTicks = tick;

            
            while (!pendingArrivals.isEmpty() && pendingArrivals.peek().getArrivalTime() <= tick) {
                Process arriving = pendingArrivals.dequeue();
                arriving.setState(ProcessState.READY);
                readyQueue.enqueue(arriving);
                System.out.println("[CPU] Tick " + tick + " -> Llega: " + arriving.getPid());
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
                    System.out.println("[CPU] Tick " + tick + " -> IDLE (sin procesos)");
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

            } else if (quantumLeft == 0) {
                currentProcess.setState(ProcessState.READY);
                readyQueue.enqueue(currentProcess);
                System.out.println("[CPU] Quantum terminado para " + currentProcess.getPid() + " -> reencolando");
                currentProcess = null;
            }

        } catch (InterruptedException e) {
            
        } finally {
            if (acquired) mutex.release();
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

        System.out.println("==========================================\n");
    }
}
