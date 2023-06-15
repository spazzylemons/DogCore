package net.dumbdogdiner.dogcore.commands;

import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.teleport.TpaManager;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public final class TpaCommands {
    private TpaCommands() { }

    @Command({"tpask", "tpa"})
    @CommandPermission(Permissions.TPA)
    public static void tpa(final Player sender, final Player player) {
        TpaManager.request(sender, player, false);
    }

    @Command("tpahere")
    @CommandPermission(Permissions.TPA)
    public static void tpaHere(final Player sender, final Player player) {
        TpaManager.request(sender, player, true);
    }

    @Command("tpaccept")
    public static void tpAccept(final Player sender, final Player player) {
        TpaManager.accept(sender, player);
    }

    @Command("tpdeny")
    public static void tpDeny(final Player sender, final Player player) {
        TpaManager.deny(sender, player);
    }
}
