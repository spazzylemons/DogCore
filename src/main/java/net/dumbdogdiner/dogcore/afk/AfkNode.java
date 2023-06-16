package net.dumbdogdiner.dogcore.afk;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

record AfkNode(long time, @NotNull Player player) {
    /**
     * Construct a node from a player.
     * @param p The player to attach to this node.
     */
    AfkNode(final @NotNull Player p) {
        this(System.currentTimeMillis(), p);
    }
}
