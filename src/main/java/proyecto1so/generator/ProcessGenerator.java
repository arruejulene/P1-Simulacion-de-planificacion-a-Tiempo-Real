package proyecto1so.generator;

import proyecto1so.model.PCB;
import proyecto1so.model.TaskType;

public class ProcessGenerator {
    private long seed;

    private int nextId;

    private int prioMin = 1, prioMax = 5;
    private int instrMin = 10, instrMax = 60;
    private int ddlMin = 50, ddlMax = 250;

    private int periodicChancePercent = 30;
    private int periodMin = 20, periodMax = 80;

    private int cpuChancePercent = 50;
    private int ioChancePercent = 40;
    private int trapChancePercent = 10;

    // Emergency config (configurable)
    private int emergencyPriority = 1;
    private int emergencyDeadlineMin = 5;
    private int emergencyDeadlineMax = 25;
    private int emergencyInstrMin = 5;
    private int emergencyInstrMax = 25;
    private TaskType emergencyTaskType = TaskType.CPU_BOUND;

    public ProcessGenerator(long seed, int startingId) {
        this.seed = seed;
        this.nextId = startingId;
        normalizeTaskChances();
    }

    private int nextInt(int bound) {
        seed = (seed * 1103515245L + 12345L) & 0x7fffffffL;
        return (int)(seed % bound);
    }

    private int randBetween(int min, int max) {
        if (max <= min) return min;
        return min + nextInt((max - min) + 1);
    }

    private void normalizeTaskChances() {
        int sum = cpuChancePercent + ioChancePercent + trapChancePercent;
        if (sum == 0) {
            cpuChancePercent = 100;
            ioChancePercent = 0;
            trapChancePercent = 0;
            return;
        }
        int cpu = cpuChancePercent * 100 / sum;
        int io = ioChancePercent * 100 / sum;
        int trap = 100 - cpu - io;
        cpuChancePercent = cpu;
        ioChancePercent = io;
        trapChancePercent = trap;
    }

    public void setPriorityRange(int min, int max) { this.prioMin = min; this.prioMax = max; }
    public void setInstructionsRange(int min, int max) { this.instrMin = min; this.instrMax = max; }
    public void setDeadlineRange(int min, int max) { this.ddlMin = min; this.ddlMax = max; }

    public void setPeriodicity(int chancePercent, int periodMin, int periodMax) {
        this.periodicChancePercent = chancePercent;
        this.periodMin = periodMin;
        this.periodMax = periodMax;
    }

    public void setTaskTypeChances(int cpuPercent, int ioPercent, int trapPercent) {
        this.cpuChancePercent = cpuPercent;
        this.ioChancePercent = ioPercent;
        this.trapChancePercent = trapPercent;
        normalizeTaskChances();
    }

    public void setEmergencyConfig(int priority, int ddlMin, int ddlMax, int instrMin, int instrMax, TaskType type) {
        this.emergencyPriority = priority;
        this.emergencyDeadlineMin = ddlMin;
        this.emergencyDeadlineMax = ddlMax;
        this.emergencyInstrMin = instrMin;
        this.emergencyInstrMax = instrMax;
        this.emergencyTaskType = type;
    }

    public PCB generateRandomProcess(long currentCycle) {
        int id = nextId++;
        String name = "P" + id;

        int priority = randBetween(prioMin, prioMax);
        int instructionsTotal = randBetween(instrMin, instrMax);
        int deadlineTotal = randBetween(ddlMin, ddlMax);

        int roll = nextInt(100);
        TaskType type;
        if (roll < cpuChancePercent) type = TaskType.CPU_BOUND;
        else if (roll < cpuChancePercent + ioChancePercent) type = TaskType.IO_BOUND;
        else type = TaskType.TRAP;

        boolean periodic = nextInt(100) < periodicChancePercent;
        int period = periodic ? randBetween(periodMin, periodMax) : 0;
        long nextRelease = periodic ? (currentCycle + period) : 0;

        return new PCB(
                id,
                name,
                priority,
                deadlineTotal,
                instructionsTotal,
                type,
                periodic,
                period,
                nextRelease,
                currentCycle
        );
    }

    public PCB[] generateRandomBatch(int n, long currentCycle) {
        if (n <= 0) return new PCB[0];
        PCB[] batch = new PCB[n];
        for (int i = 0; i < n; i++) {
            batch[i] = generateRandomProcess(currentCycle);
        }
        return batch;
    }

    public PCB generateEmergencyProcess(long currentCycle) {
        int id = nextId++;
        String name = "EMERGENCY-" + id;

        int priority = emergencyPriority;
        int instructionsTotal = randBetween(emergencyInstrMin, emergencyInstrMax);
        int deadlineTotal = randBetween(emergencyDeadlineMin, emergencyDeadlineMax);

        return new PCB(
                id,
                name,
                priority,
                deadlineTotal,
                instructionsTotal,
                emergencyTaskType,
                false,
                0,
                0,
                currentCycle
        );
    }
}

