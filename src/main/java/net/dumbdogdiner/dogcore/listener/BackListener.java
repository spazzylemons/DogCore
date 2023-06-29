package net.dumbdogdiner.dogcore.listener;

import net.dumbdogdiner.dogcore.DogCorePlugin;
import net.dumbdogdiner.dogcore.teleport.LocationDataType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for teleport events and handles the player's saved /back location.
 * Data is stored persistently, meaning players can access /back between logins.
 */
public final class BackListener implements Listener {
    /** The key used to access the data. */
    private static final NamespacedKey LAST_LOCATION = DogCorePlugin.key("last-location");

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(final @NotNull PlayerTeleportEvent event) {
        var player = event.getPlayer();
        switch (event.getCause()) {
            case PLUGIN, COMMAND -> {
                var container = player.getPersistentDataContainer();
                // store the last location in the container
                container.set(LAST_LOCATION, LocationDataType.INSTANCE, event.getFrom());
            }

            default -> { }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(final @NotNull PlayerDeathEvent event) {
        var player = event.getPlayer();
        var container = player.getPersistentDataContainer();
        container.set(LAST_LOCATION, LocationDataType.INSTANCE, player.getLocation());
    }

    /**
     * Get a player's /back location.
     * @param player The player.
     * @return The /back location, or null if none exists.
     */
    public static @Nullable Location getBack(final @NotNull Player player) {
        var container = player.getPersistentDataContainer();
        return container.get(LAST_LOCATION, LocationDataType.INSTANCE);
    }
}
