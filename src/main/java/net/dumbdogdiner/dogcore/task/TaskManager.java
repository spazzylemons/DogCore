package net.dumbdogdiner.dogcore.task;

import java.util.function.Consumer;
import net.dumbdogdiner.dogcore.DogCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

public final class TaskManager {
    private TaskManager() { }

    /** The scheduler. */
    private static final BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    /** The plugin. */
    private static final Plugin PLUGIN = DogCorePlugin.getInstance();

    public static void async(final @NotNull TaskFrequency frequency, final @NotNull Runnable task) {
        SCHEDULER.runTaskTimerAsynchronously(PLUGIN, task, 0L, frequency.getTicks());
    }

    public static void players(
        final @NotNull TaskFrequency frequency,
        final @NotNull Object lock,
        final @NotNull Consumer<Player> task
    ) {
        async(frequency, new PlayerRunnable(lock, task));
    }
}
