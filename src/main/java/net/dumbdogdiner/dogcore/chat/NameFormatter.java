package net.dumbdogdiner.dogcore.chat;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class NameFormatter {
    private NameFormatter() {}

    private static final LuckPerms LUCKPERMS = LuckPermsProvider.get();

    @SuppressWarnings("resource")
    public static void init(@NotNull JavaPlugin plugin) {
        LUCKPERMS.getEventBus().subscribe(plugin, GroupDataRecalculateEvent.class, event -> {
            var onlinePlayers = Bukkit.getOnlinePlayers();
            var futures = new CompletableFuture[onlinePlayers.size()];
            var i = 0;
            for (var player : onlinePlayers) {
                futures[i++] = refreshPlayerName(player);
            }
            CompletableFuture.allOf(futures).join();
        });
    }

    private static @NotNull TextColor parseColor(@NotNull String color) {
        var result = TextColor.fromCSSHexString(color);
        if (result == null) {
            result = NamedTextColor.NAMES.value(color);
            if (result == null) {
                // default to white if parsing fails
                result = NamedTextColor.WHITE;
            }
        }
        return result;
    }

    private static @NotNull CompletableFuture<@NotNull User> getOrLoadUser(@NotNull UUID uuid, @NotNull String name) {
        var userManager = LUCKPERMS.getUserManager();
        var loadedUser = userManager.getUser(uuid);
        if (loadedUser == null) {
            return userManager.loadUser(uuid, name);
        }
        return CompletableFuture.completedFuture(loadedUser);
    }

    public static @NotNull CompletableFuture<@NotNull Component> formatUsername(@NotNull UUID uuid, @NotNull String name) {
        return getOrLoadUser(uuid, name).thenApply(user -> {
            TextColor color = NamedTextColor.WHITE;
            String rank = null;
            // TODO what if the group isn't loaded either? Shouldn't we load it?
            var group = LUCKPERMS.getGroupManager().getGroup(user.getPrimaryGroup());
            if (group != null) {
                var metadata = group.getCachedData().getMetaData();
                var textColor = metadata.getMetaValue("color");
                if (textColor != null) {
                    color = parseColor(textColor);
                }
                rank = metadata.getMetaValue("rank");
            }
            var text = (rank != null) ? "[" + rank + "] " + name : name;
            return Component.text(text, color).insertion(name);
        });
    }

    public static @NotNull CompletableFuture<@NotNull Component> formatUsername(@NotNull Player player) {
        var user = net.dumbdogdiner.dogcore.db.User.Companion.lookup(player);
        if (user != null) {
            return user.formattedName();
        } else {
            return formatUsername(player.getUniqueId(), player.getName());
        }
    }

    public static @NotNull CompletableFuture<Void> refreshPlayerName(@NotNull Player player) {
        return formatUsername(player).thenApply(name -> {
            player.displayName(name);
            player.playerListName(name);
            return null;
        });
    }
}
