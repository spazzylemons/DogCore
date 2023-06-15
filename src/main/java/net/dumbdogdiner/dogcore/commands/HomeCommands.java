package net.dumbdogdiner.dogcore.commands;

import net.dumbdogdiner.dogcore.DogCorePlugin;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.dumbdogdiner.dogcore.teleport.LocationDataType;
import net.dumbdogdiner.dogcore.teleport.SafeTeleport;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public final class HomeCommands {
    private HomeCommands() { }

    /** The key for the home location. */
    private static final NamespacedKey HOME_KEY = DogCorePlugin.key("home");

    private static @Nullable Location getHome(@NotNull final Player player) {
        return player.getPersistentDataContainer().get(HOME_KEY, LocationDataType.INSTANCE);
    }

    @Command("home")
    @CommandPermission(Permissions.HOME)
    public static void home(final Player sender) {
        var home = getHome(sender);
        if (home != null) {
            SafeTeleport.safeTeleport(sender, home);
            sender.sendMessage(Messages.get("commands.home.success"));
        } else {
            sender.sendMessage(Messages.get("commands.home.noHome"));
        }
    }

    @Command("sethome")
    @CommandPermission(Permissions.HOME)
    public static void setHome(final Player sender) {
        sender.getPersistentDataContainer().set(HOME_KEY, LocationDataType.INSTANCE, sender.getLocation());
        sender.sendMessage(Messages.get("commands.sethome.success"));
    }

    @Command("delhome")
    @CommandPermission(Permissions.HOME)
    public static void delHome(final Player sender) {
        var container = sender.getPersistentDataContainer();
        if (container.has(HOME_KEY)) {
            container.remove(HOME_KEY);
            sender.sendMessage(Messages.get("commands.delhome.success"));
        } else {
            sender.sendMessage(Messages.get("commands.home.noHome"));
        }
    }
}
