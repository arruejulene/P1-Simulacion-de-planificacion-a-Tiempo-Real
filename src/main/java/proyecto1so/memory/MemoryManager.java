package proyecto1so.memory;

import proyecto1so.datastructures.Queue;
import proyecto1so.model.PCB;
import proyecto1so.model.ProcessState;
import proyecto1so.model.StateManager;

public class MemoryManager {
    private final int maxInRam;
    private int inRamCount;

    private final SuspensionPolicy policy;

    private final Queue<PCB> readyInRam;
    private final Queue<PCB> blockedInRam;
    private final Queue<PCB> readySuspended;
    private final Queue<PCB> blockedSuspended;

    public MemoryManager(int maxInRam) {
        this(maxInRam, SuspensionPolicy.LOWEST_PRIORITY);
    }

    public MemoryManager(int maxInRam, SuspensionPolicy policy) {
        this.maxInRam = maxInRam;
        this.inRamCount = 0;
        this.policy = policy;

        this.readyInRam = new Queue<>();
        this.blockedInRam = new Queue<>();
        this.readySuspended = new Queue<>();
        this.blockedSuspended = new Queue<>();
    }

    public int getMaxInRam() { return maxInRam; }
    public int getInRamCount() { return inRamCount; }

    public Queue<PCB> getReadyInRam() { return readyInRam; }
    public Queue<PCB> getBlockedInRam() { return blockedInRam; }
    public Queue<PCB> getReadySuspended() { return readySuspended; }
    public Queue<PCB> getBlockedSuspended() { return blockedSuspended; }

    public boolean admitProcess(PCB pcb) {
        if (pcb == null) return false;

        if (inRamCount >= maxInRam) {
            System.out.println("[RAM FULL] Trying to suspend one process to admit: " +
                    pcb.getName() + " (id=" + pcb.getId() + ")");
            boolean freed = suspendOne();

            if (!freed) {
                StateManager.transition(pcb, ProcessState.READY_SUSPENDED);
                readySuspended.enqueue(pcb);
                System.out.println("[NO VICTIM FOUND] Sent to READY_SUSPENDED: " +
                        pcb.getName() + " (id=" + pcb.getId() + ")");
                return false;
            }
        }

        StateManager.transition(pcb, ProcessState.READY);
        readyInRam.enqueue(pcb);
        inRamCount++;
        System.out.println("[ADMITTED TO RAM] " + pcb.getName() + " (id=" + pcb.getId() +
                ") inRamCount=" + inRamCount + "/" + maxInRam);
        return true;
    }

    public boolean suspendOne() {
        if (inRamCount <= 0) return false;

        PCB victimReady = pickVictimFromQueue(readyInRam);
        if (victimReady != null) {
            StateManager.transition(victimReady, ProcessState.READY_SUSPENDED);
            readySuspended.enqueue(victimReady);
            inRamCount--;
            System.out.println("[SUSPENDED] READY -> READY_SUSPENDED: " +
                    victimReady.getName() + " (id=" + victimReady.getId() +
                    ") inRamCount=" + inRamCount + "/" + maxInRam);
            return true;
        }

        PCB victimBlocked = pickVictimFromQueue(blockedInRam);
        if (victimBlocked != null) {
            StateManager.transition(victimBlocked, ProcessState.BLOCKED_SUSPENDED);
            blockedSuspended.enqueue(victimBlocked);
            inRamCount--;
            System.out.println("[SUSPENDED] BLOCKED -> BLOCKED_SUSPENDED: " +
                    victimBlocked.getName() + " (id=" + victimBlocked.getId() +
                    ") inRamCount=" + inRamCount + "/" + maxInRam);
            return true;
        }

        return false;
    }

    public boolean swapInOneIfPossible() {
        if (inRamCount >= maxInRam) return false;

        PCB candidate = readySuspended.dequeue();
        if (candidate != null) {
            StateManager.transition(candidate, ProcessState.READY);
            readyInRam.enqueue(candidate);
            inRamCount++;
            System.out.println("[SWAP IN] READY_SUSPENDED -> READY: " +
                    candidate.getName() + " (id=" + candidate.getId() +
                    ") inRamCount=" + inRamCount + "/" + maxInRam);
            return true;
        }

        PCB candBlocked = blockedSuspended.dequeue();
        if (candBlocked != null) {
            StateManager.transition(candBlocked, ProcessState.BLOCKED);
            blockedInRam.enqueue(candBlocked);
            inRamCount++;
            System.out.println("[SWAP IN] BLOCKED_SUSPENDED -> BLOCKED: " +
                    candBlocked.getName() + " (id=" + candBlocked.getId() +
                    ") inRamCount=" + inRamCount + "/" + maxInRam);
            return true;
        }

        return false;
    }

    private PCB pickVictimFromQueue(Queue<PCB> q) {
        if (q == null || q.isEmpty()) return null;

        Queue<PCB> temp = new Queue<>();
        PCB victim = null;

        int n = q.size();
        for (int i = 0; i < n; i++) {
            PCB p = q.dequeue();
            if (p == null) break;

            if (victim == null) victim = p;
            else if (isWorse(p, victim)) victim = p;

            temp.enqueue(p);
        }

        boolean removedOnce = false;
        int m = temp.size();
        for (int i = 0; i < m; i++) {
            PCB p = temp.dequeue();
            if (p == null) break;

            if (!removedOnce && p == victim) {
                removedOnce = true;
                continue;
            }
            q.enqueue(p);
        }

        return victim;
    }

    private boolean isWorse(PCB a, PCB b) {
        if (a == null) return false;
        if (b == null) return true;

        switch (policy) {
            case LOWEST_PRIORITY:
                return a.getPriority() > b.getPriority();

            case FARTHEST_DEADLINE:
                return a.getDeadlineRemaining() > b.getDeadlineRemaining();

            default:
                return false;
        }
    }
}
