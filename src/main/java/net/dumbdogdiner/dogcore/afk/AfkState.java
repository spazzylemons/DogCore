package net.dumbdogdiner.dogcore.afk;

import net.dumbdogdiner.dogcore.util.LinkedQueue;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record AfkState(
    @NotNull Location lastLocation,
    LinkedQueue.@Nullable Node<@NotNull AfkNode> timeoutNode
) {
    /**
     * Construct a default AFK state from a player.
     * @param player The player to get a location from.
     */
    AfkState(final @NotNull Player player, final LinkedQueue.@Nullable Node<@NotNull AfkNode> node) {
        this(player.getLocation(), node);
    }

    /**
     * @return True if the state indicates that the player is AFK.
     */
    boolean isAfk() {
        return timeoutNode == null;
    }
}
