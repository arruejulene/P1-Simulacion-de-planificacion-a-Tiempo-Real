package proyecto1so.io;

import proyecto1so.cpu.CPUScheduler;

public class IOEventGenerator extends Thread {

    private final CPUScheduler cpu;
    private final int minIntervalMs;
    private final int maxIntervalMs;
    private final int minServiceTicks;
    private final int maxServiceTicks;

    private volatile boolean running = true;
    private long seed;
    private int eventCounter = 1;

    public IOEventGenerator(CPUScheduler cpu,
                            int minIntervalMs,
                            int maxIntervalMs,
                            int minServiceTicks,
                            int maxServiceTicks,
                            long seed) {
        this.cpu = cpu;
        this.minIntervalMs = minIntervalMs;
        this.maxIntervalMs = maxIntervalMs;
        this.minServiceTicks = minServiceTicks;
        this.maxServiceTicks = maxServiceTicks;
        this.seed = seed;
        setName("IOEventGenerator");
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

            int serviceTicks = randBetween(minServiceTicks, maxServiceTicks);
            String reason = "IO-" + eventCounter++;
            cpu.triggerIOEvent(reason, serviceTicks);
        }
    }

    private void safeSleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // stopGenerator() interrupts sleep intentionally.
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
