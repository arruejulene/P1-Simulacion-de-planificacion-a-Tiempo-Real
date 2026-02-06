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
import proyecto1so.scheduler.RoundRobinStrategy;
import proyecto1so.scheduler.SchedulerStrategy;

public class CPUScheduler implements ClockListener {

    
    private final Semaphore mutex = new Semaphore(1, true);


    private final Queue<Process> readyQueue = new Queue<>();


    private final OrderedQueue<Process> pendingArrivals = new OrderedQueue<>(new Compare<Process>() {
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

    

    public void setStrategy(SchedulerStrategy strategy) {
        if (strategy != null) {
            this.strategy = strategy;
        }
    }

    public void addProcess(Process p) {
        if (p == null) return;

        
        Process toInsert = p;
        if (p.getArrivalTime() <= 0) {
            int arrival = 1 + (pendingArrivals.size() * 2);
            toInsert = new Process(p.getPid(), p.getBurstTime(), arrival);
        }

        pendingArrivals.insertOrdered(toInsert);
    }

   

    @Override
    public void onTick(int tick) {
        try {
            mutex.acquire();

            totalTicks = tick;

           
            while (!pendingArrivals.isEmpty() && pendingArrivals.peek().getArrivalTime() <= tick) {
                Process arriving = pendingArrivals.dequeue();
                arriving.setState(ProcessState.READY);
                readyQueue.enqueue(arriving);
                System.out.println("[CPU] Tick " + tick + " -> Llega: " + arriving.getPid());
            }

           
            if (currentProcess == null) {
                currentProcess = strategy.selectNextProcess(readyQueue);

                if (currentProcess != null) {
                    currentProcess.setState(ProcessState.RUNNING);
                    currentProcess.markFirstRun(tick);

                    quantumLeft = strategy.getQuantum();
                    System.out.println("[CPU] Tick " + tick + " -> Ejecutando: " + currentProcess.getPid());
                } else {
                    
                    if (pendingArrivals.isEmpty()) {
                        System.out.println("[CPU] Tick " + tick + " -> IDLE (sin procesos)");
                    }
                    return;
                }
            }

            
            busyTicks++;

            currentProcess.consumeOneTick();
            System.out.println("    [Process " + currentProcess.getPid() + "] tiempo restante: " + currentProcess.getRemainingTime());

            
            if (quantumLeft != Integer.MAX_VALUE) {
                quantumLeft--;
            }

            System.out.println("[CPU] Tick " + tick + " -> " + currentProcess.getPid()
                    + " restante=" + currentProcess.getRemainingTime()
                    + " quantumLeft=" + (quantumLeft == Integer.MAX_VALUE ? "INF" : quantumLeft));

            
            if (currentProcess.isFinished()) {
                currentProcess.markFinish(tick);
                currentProcess.setState(ProcessState.TERMINATED);

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
            mutex.release();
        }
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
