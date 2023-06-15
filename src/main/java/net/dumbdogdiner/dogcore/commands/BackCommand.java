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
    private BackCommand() { }

    /**
     * The death locations of players.
     */
    private static final Map<@NotNull UUID, @NotNull Location> DEATH_LOCATIONS
        = new HashMap<>();

    /**
     * The /back command.
     * @param player The player sending the command.
     */
    @Command("back")
    @CommandPermission(Permissions.BACK)
    public static void back(final Player player) {
        var location = removeBack(player.getUniqueId());
        if (location == null) {
            player.sendMessage(Messages.get("commands.back.nothing"));
        } else {
            player.sendMessage(Messages.get("commands.back.success"));
            SafeTeleport.safeTeleport(player, location);
        }
    }

    /**
     * Set a player's back location.
     * @param uuid The player's unique ID.
     * @param location The location to insert.
     */
    public static synchronized void setBack(
        @NotNull final UUID uuid,
        @NotNull final Location location
    ) {
        DEATH_LOCATIONS.put(uuid, location);
    }

    /**
     * Remove a player's back location.
     * @param uuid The player's unique ID.
     * @return The location that was removed.
     */
    public static synchronized @Nullable Location removeBack(@NotNull final UUID uuid) {
        return DEATH_LOCATIONS.remove(uuid);
    }
}
