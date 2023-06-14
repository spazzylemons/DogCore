package net.dumbdogdiner.dogcore.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.dumbdogdiner.dogcore.teleport.SafeTeleport;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public final class BackCommand {
    private BackCommand() {}

    private static final Map<@NotNull UUID, @NotNull Location> deathLocations = new HashMap<>();

    @Command("back")
    @CommandPermission(Permissions.BACK)
    public static void back(Player player) {
        var location = removeBack(player.getUniqueId());
        if (location == null) {
            player.sendMessage(Messages.get("commands.back.nothing"));
        } else {
            player.sendMessage(Messages.get("commands.back.success"));
            SafeTeleport.safeTeleport(player, location);
        }
    }

    public static synchronized void setBack(@NotNull UUID uuid, @NotNull Location location) {
        deathLocations.put(uuid, location);
    }

    public static synchronized @Nullable Location removeBack(@NotNull UUID uuid) {
        return deathLocations.remove(uuid);
    }
}
