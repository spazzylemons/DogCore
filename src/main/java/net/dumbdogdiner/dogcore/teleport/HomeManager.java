package net.dumbdogdiner.dogcore.teleport;

import net.dumbdogdiner.dogcore.DogCorePlugin;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HomeManager {

    /** The key for the home location. */
    private static final NamespacedKey HOME_KEY = DogCorePlugin.key("home");

    private HomeManager() { }

    public static synchronized @Nullable Location getHome(final @NotNull Player player) {
        return player.getPersistentDataContainer().get(HOME_KEY, LocationDataType.INSTANCE);
    }

    public static synchronized void setHome(final @NotNull Player player, final @NotNull Location location) {
        player.getPersistentDataContainer().set(HOME_KEY, LocationDataType.INSTANCE, location);
    }

    public static synchronized boolean delHome(final @NotNull Player player) {
        var container = player.getPersistentDataContainer();
        if (container.has(HOME_KEY)) {
            container.remove(HOME_KEY);
            return true;
        }
        return false;
    }
}
