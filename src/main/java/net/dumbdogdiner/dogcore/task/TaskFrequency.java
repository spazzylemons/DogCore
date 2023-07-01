package net.dumbdogdiner.dogcore.task;

/** The frequency of a task. */
public enum TaskFrequency {
    /** The task will run every second. */
    HIGH(1),
    /** The task will run every ten seconds. */
    LOW(10);

    /** The ticks that this task will take. */
    private final int ticks;

    /** The number of ticks in a second. */
    private static final int TICKS_PER_SECOND = 20;

    TaskFrequency(final int seconds) {
        ticks = seconds * TICKS_PER_SECOND;
    }

    public int getTicks() {
        return ticks;
    }
}
