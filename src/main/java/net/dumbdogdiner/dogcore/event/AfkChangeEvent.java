package net.dumbdogdiner.dogcore.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public final class AfkChangeEvent extends PlayerEvent {
    /** The list of handlers. */
    private static final HandlerList HANDLERS = new HandlerList();

    public AfkChangeEvent(final @NotNull Player player) {
        super(player);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
