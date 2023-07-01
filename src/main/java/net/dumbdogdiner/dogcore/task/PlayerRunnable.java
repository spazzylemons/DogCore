package net.dumbdogdiner.dogcore.task;

import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

final class PlayerRunnable implements Runnable {
    /** The lock to hold when iterating over the players. */
    private final @NotNull Object lock;

    /** The task to run on each player. */
    private final @NotNull Consumer<Player> consumer;

    PlayerRunnable(final @NotNull Object l, final @NotNull Consumer<Player> c) {
        lock = l;
        consumer = c;
    }

    @Override
    public void run() {
        synchronized (lock) {
            for (var player : Bukkit.getOnlinePlayers()) {
                consumer.accept(player);
            }
        }
    }
}
