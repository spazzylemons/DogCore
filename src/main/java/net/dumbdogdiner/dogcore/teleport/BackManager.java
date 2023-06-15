package net.dumbdogdiner.dogcore.teleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for teleport events and handles the player's saved /back location.
 * Data is stored persistently, meaning players can access /back between logins.
 * TODO: is that a good idea? couldn't it be abused?
 */
public final class BackManager implements Listener {
    private BackManager() { }

    /** The key used to access the data. */
    private static NamespacedKey lastLocationKey;

    /** The string constant used for the key. */
    private static final String LAST_LOCATION = "last-location";

    /**
     * Create a new back manager.
     * @param plugin The plugin instance.
     */
    public static void init(@NotNull final Plugin plugin) {
        lastLocationKey = new NamespacedKey(plugin, LAST_LOCATION);
        Bukkit.getPluginManager().registerEvents(new BackManager(), plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(@NotNull final PlayerTeleportEvent event) {
        var player = event.getPlayer();
        switch (event.getCause()) {
            case PLUGIN, COMMAND -> {
                var container = player.getPersistentDataContainer();
                // store the last location in the container
                container.set(lastLocationKey, LocationDataType.INSTANCE, event.getFrom());
            }

            default -> { }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(@NotNull final PlayerDeathEvent event) {
        var player = event.getPlayer();
        var container = player.getPersistentDataContainer();
        container.set(lastLocationKey, LocationDataType.INSTANCE, player.getLocation());
    }

    /**
     * Get a player's /back location.
     * @param player The player.
     * @return The /back location, or null if none exists.
     */
    public static @Nullable Location getBack(@NotNull final Player player) {
        var container = player.getPersistentDataContainer();
        return container.get(lastLocationKey, LocationDataType.INSTANCE);
    }
}
