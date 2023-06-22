package net.dumbdogdiner.dogcore.commands.gui;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import net.dumbdogdiner.dogcore.Permissions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("craft")
@Permission(Permissions.CRAFT)
public final class CraftCommand {
    private CraftCommand() { }

    @Default
    public static void craft(final @NotNull Player sender) {
        sender.openWorkbench(null, false);
    }
}
