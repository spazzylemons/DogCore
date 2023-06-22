package net.dumbdogdiner.dogcore.commands.snoop;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import net.dumbdogdiner.dogcore.Permissions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("invsee")
@Permission(Permissions.SNOOP)
public final class InvSeeCommand {
    private InvSeeCommand() { }

    @Default
    public static void invSee(final @NotNull Player sender, final @NotNull @APlayerArgument Player player) {
        sender.openInventory(player.getInventory());
    }
}
