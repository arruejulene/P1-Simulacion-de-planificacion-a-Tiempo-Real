package proyecto1so.interrupts;

import proyecto1so.cpu.CPUScheduler;

public class InterruptGenerator extends Thread {

    private final CPUScheduler cpu;
    private final int minIntervalMs;
    private final int maxIntervalMs;
    private final int minIsrTicks;
    private final int maxIsrTicks;

    private volatile boolean running = true;
    private long seed;
    private int eventCounter = 1;

    public InterruptGenerator(CPUScheduler cpu,
                              int minIntervalMs,
                              int maxIntervalMs,
                              int minIsrTicks,
                              int maxIsrTicks,
                              long seed) {
        this.cpu = cpu;
        this.minIntervalMs = minIntervalMs;
        this.maxIntervalMs = maxIntervalMs;
        this.minIsrTicks = minIsrTicks;
        this.maxIsrTicks = maxIsrTicks;
        this.seed = seed;
        setName("InterruptGenerator");
    }

    public void stopGenerator() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        while (running) {
            int delay = randBetween(minIntervalMs, maxIntervalMs);
            safeSleep(delay);
            if (!running) break;

            int isrTicks = randBetween(minIsrTicks, maxIsrTicks);
            String irqName = "EXT-" + eventCounter++;
            cpu.triggerExternalInterrupt(irqName, isrTicks);
        }
    }

    private void safeSleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // stopGenerator() interrupts sleep on purpose.
        }
    }

    private int randBetween(int min, int max) {
        if (max <= min) return min;
        int bound = (max - min) + 1;
        return min + nextInt(bound);
    }

    private int nextInt(int bound) {
        seed = (seed * 1103515245L + 12345L) & 0x7fffffffL;
        return (int) (seed % bound);
    }
}
