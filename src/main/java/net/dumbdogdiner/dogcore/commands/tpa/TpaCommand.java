package net.dumbdogdiner.dogcore.commands.tpa;

import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.arguments.APlayerArgument;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.teleport.TpaManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("tpa")
@Alias("tpask")
@Permission(Permissions.TPA)
public final class TpaCommand {
    private TpaCommand() { }

    @Default
    public static void tpa(
            final @NotNull Player sender,
            final @NotNull @APlayerArgument Player player
    ) {
        TpaManager.request(sender, player, false);
    }
}
