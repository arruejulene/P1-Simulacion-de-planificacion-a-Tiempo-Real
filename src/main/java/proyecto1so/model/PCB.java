package proyecto1so.model;

public class PCB {
    private final int id;
    private final String name;

    private ProcessState state;

    private int pc;
    private int mar;

    private int priority;

    private int deadlineTotal;
    private int deadlineRemaining;

    private int instructionsTotal;
    private int instructionsRemaining;

    private TaskType taskType;

    private boolean periodic;
    private int period;
    private long nextRelease;

    private long arrivalCycle;
    private long startCycle;
    private long finishCycle;
    private long waitingCycles;

    public PCB(int id,
               String name,
               int priority,
               int deadlineTotal,
               int instructionsTotal,
               TaskType taskType,
               boolean periodic,
               int period,
               long nextRelease,
               long arrivalCycle) {
        this.id = id;
        this.name = name;

        this.state = ProcessState.NEW;

        this.pc = 0;
        this.mar = 0;

        this.priority = priority;

        this.deadlineTotal = deadlineTotal;
        this.deadlineRemaining = deadlineTotal;

        this.instructionsTotal = instructionsTotal;
        this.instructionsRemaining = instructionsTotal;

        this.taskType = taskType;

        this.periodic = periodic;
        this.period = period;
        this.nextRelease = nextRelease;

        this.arrivalCycle = arrivalCycle;
        this.startCycle = -1;
        this.finishCycle = -1;
        this.waitingCycles = 0;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    public ProcessState getState() { return state; }
    public void setState(ProcessState state) { this.state = state; }

    public int getPc() { return pc; }
    public void incPc() { this.pc++; }

    public int getMar() { return mar; }
    public void incMar() { this.mar++; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public int getDeadlineTotal() { return deadlineTotal; }
    public int getDeadlineRemaining() { return deadlineRemaining; }
    public void setDeadlineRemaining(int v) { this.deadlineRemaining = v; }
    public void decDeadlineRemaining() { this.deadlineRemaining--; }

    public int getInstructionsTotal() { return instructionsTotal; }
    public int getInstructionsRemaining() { return instructionsRemaining; }
    public void setInstructionsRemaining(int v) { this.instructionsRemaining = v; }
    public void decInstructionsRemaining() { this.instructionsRemaining--; }

    public TaskType getTaskType() { return taskType; }
    public void setTaskType(TaskType taskType) { this.taskType = taskType; }

    public boolean isPeriodic() { return periodic; }
    public void setPeriodic(boolean periodic) { this.periodic = periodic; }

    public int getPeriod() { return period; }
    public void setPeriod(int period) { this.period = period; }

    public long getNextRelease() { return nextRelease; }
    public void setNextRelease(long nextRelease) { this.nextRelease = nextRelease; }

    public long getArrivalCycle() { return arrivalCycle; }
    public void setArrivalCycle(long arrivalCycle) { this.arrivalCycle = arrivalCycle; }

    public long getStartCycle() { return startCycle; }
    public void setStartCycle(long startCycle) { this.startCycle = startCycle; }

    public long getFinishCycle() { return finishCycle; }
    public void setFinishCycle(long finishCycle) { this.finishCycle = finishCycle; }

    public long getWaitingCycles() { return waitingCycles; }
    public void incWaitingCycles() { this.waitingCycles++; }
    public void addWaitingCycles(long delta) { this.waitingCycles += delta; }

    public boolean isFinished() { return instructionsRemaining <= 0; }

    public void resetForNewRelease(long newArrivalCycle) {
        this.state = ProcessState.NEW;
        this.pc = 0;
        this.mar = 0;
        this.deadlineRemaining = this.deadlineTotal;
        this.instructionsRemaining = this.instructionsTotal;
        this.arrivalCycle = newArrivalCycle;
        this.startCycle = -1;
        this.finishCycle = -1;
        this.waitingCycles = 0;
    }

    private String stateFullName(ProcessState s) {
        if (s == null) return "Unknown";
        switch (s) {
            case NEW: return "New";
            case READY: return "Ready";
            case RUNNING: return "Running";
            case BLOCKED: return "Blocked";
            case TERMINATED: return "Terminated";
            case READY_SUSPENDED: return "Ready Suspended";
            case BLOCKED_SUSPENDED: return "Blocked Suspended";
            default: return "Unknown";
        }
    }

    private String taskTypeFullName(TaskType t) {
        if (t == null) return "Unknown";
        switch (t) {
            case CPU_BOUND: return "CPU Bound";
            case IO_BOUND: return "I/O Bound";
            case TRAP: return "Trap";
            default: return "Unknown";
        }
    }

    @Override
    public String toString() {
        return "Process Control Block {" +
                " Process ID=" + id +
                ", Process Name='" + name + '\'' +
                ", State=" + stateFullName(state) +
                ", Task Type=" + taskTypeFullName(taskType) +
                ", Program Counter (PC)=" + pc +
                ", Memory Address Register (MAR)=" + mar +
                ", Priority=" + priority +
                ", Deadline Remaining/Total=" + deadlineRemaining + "/" + deadlineTotal +
                ", Instructions Remaining/Total=" + instructionsRemaining + "/" + instructionsTotal +
                ", Periodic=" + periodic +
                ", Period=" + period +
                ", Next Release Cycle=" + nextRelease +
                ", Arrival Cycle=" + arrivalCycle +
                ", Start Cycle=" + startCycle +
                ", Finish Cycle=" + finishCycle +
                ", Waiting Cycles=" + waitingCycles +
                " }";
    }
}
