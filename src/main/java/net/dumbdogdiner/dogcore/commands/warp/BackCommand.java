package net.dumbdogdiner.dogcore.commands.warp;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.dumbdogdiner.dogcore.teleport.BackManager;
import net.dumbdogdiner.dogcore.teleport.TeleportHelper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("back")
@Permission(Permissions.BACK)
public final class BackCommand {
    private BackCommand() { }

    @Default
    public static void back(final @NotNull Player player) {
        var location = BackManager.getBack(player);
        if (location == null) {
            player.sendMessage(Messages.get("commands.back.nothing"));
        } else {
            player.sendMessage(Messages.get("commands.back.success"));
            TeleportHelper.safeTeleport(player, location);
        }
    }
}
