package net.dumbdogdiner.dogcore.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.afk.AfkManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public final class AfkCommand {
    private AfkCommand() {}

    @Command("afk")
    public static void afk(Player player) {
        AfkManager.toggleAfk(player.getUniqueId(), player.getLocation());
    }
}
