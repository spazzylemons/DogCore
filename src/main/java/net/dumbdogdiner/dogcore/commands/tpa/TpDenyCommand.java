package net.dumbdogdiner.dogcore.commands.tpa;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.teleport.TpaManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("tpdeny")
@Permission(Permissions.TPA)
public final class TpDenyCommand {
    private TpDenyCommand() { }

    @Default
    public static void tpDeny(
            final @NotNull Player sender,
            final @NotNull @APlayerArgument Player player
    ) {
        TpaManager.deny(sender, player);
    }
}
