package net.dumbdogdiner.dogcore.commands;

import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.dumbdogdiner.dogcore.teleport.BackManager;
import net.dumbdogdiner.dogcore.teleport.SafeTeleport;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public final class BackCommand {
    private BackCommand() { }

    /**
     * The /back command.
     * @param player The player sending the command.
     */
    @Command("back")
    @CommandPermission(Permissions.BACK)
    public static void back(final Player player) {
        var location = BackManager.getBack(player);
        if (location == null) {
            player.sendMessage(Messages.get("commands.back.nothing"));
        } else {
            player.sendMessage(Messages.get("commands.back.success"));
            SafeTeleport.safeTeleport(player, location);
        }
    }
}
