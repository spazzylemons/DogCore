package net.dumbdogdiner.dogcore.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player that is joining has not logged on so far today. Used for daily quests and rewards.
 */
public final class DailyLoginEvent extends PlayerEvent {
    /** The list of handlers. */
    private static final HandlerList HANDLERS = new HandlerList();

    public DailyLoginEvent(final @NotNull Player player) {
        super(player, true);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
