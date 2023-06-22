package net.dumbdogdiner.dogcore.commands.tpa;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.teleport.TpaManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("tpaccept")
@Permission(Permissions.TPA)
public final class TpAcceptCommand {
    private TpAcceptCommand() { }

    @Default
    public static void tpAccept(
            final @NotNull Player sender,
            final @NotNull @APlayerArgument Player player
    ) {
        TpaManager.accept(sender, player);
    }
}
