package net.dumbdogdiner.dogcore.commands.gui;

import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import net.dumbdogdiner.dogcore.Permissions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("enderchest")
@Alias("echest")
@Permission(Permissions.ENDER_CHEST)
public final class EnderChestCommand {
    private EnderChestCommand() { }

    @Default
    public static void enderChest(final @NotNull Player sender) {
        sender.openInventory(sender.getEnderChest());
    }
}
