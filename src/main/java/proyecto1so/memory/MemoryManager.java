package proyecto1so.memory;

import proyecto1so.datastructures.Queue;
import proyecto1so.model.PCB;
import proyecto1so.model.ProcessState;
import proyecto1so.model.StateManager;

public class MemoryManager {
    private final int maxInRam;
    private int inRamCount;

    private final Queue<PCB> readyInRam;
    private final Queue<PCB> blockedInRam;
    private final Queue<PCB> readySuspended;
    private final Queue<PCB> blockedSuspended;

    public MemoryManager(int maxInRam) {
        this.maxInRam = maxInRam;
        this.inRamCount = 0;

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

        if (inRamCount < maxInRam) {
            StateManager.transition(pcb, ProcessState.READY);
            readyInRam.enqueue(pcb);
            inRamCount++;
            return true;
        } else {
            StateManager.transition(pcb, ProcessState.READY_SUSPENDED);
            readySuspended.enqueue(pcb);
            return false;
        }
    }

    public boolean suspendOne() {
        if (inRamCount <= 0) return false;

        PCB victim = readyInRam.dequeue();
        if (victim == null) return false;

        StateManager.transition(victim, ProcessState.READY_SUSPENDED);
        readySuspended.enqueue(victim);
        inRamCount--;
        return true;
    }

    public boolean swapInOneIfPossible() {
        if (inRamCount >= maxInRam) return false;

        PCB candidate = readySuspended.dequeue();
        if (candidate == null) return false;

        StateManager.transition(candidate, ProcessState.READY);
        readyInRam.enqueue(candidate);
        inRamCount++;
        return true;
    }
}

