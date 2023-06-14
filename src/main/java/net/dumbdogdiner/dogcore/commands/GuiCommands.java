package net.dumbdogdiner.dogcore.commands;

import net.dumbdogdiner.dogcore.Permissions;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public final class GuiCommands {
    private GuiCommands() {}

    @Command({"enderchest", "echest"})
    @CommandPermission(Permissions.ENDER_CHEST)
    public static void enderChest(Player player) {
        player.openInventory(player.getEnderChest());
    }

    @Command("craft")
    @CommandPermission(Permissions.CRAFT)
    public static void craft(Player player) {
        player.openWorkbench(null, false);
    }
}
