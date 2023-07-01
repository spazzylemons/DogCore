package net.dumbdogdiner.dogcore.chat;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import net.dumbdogdiner.dogcore.DogCorePlugin;
import net.dumbdogdiner.dogcore.event.DisplayNameChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("resource")
public final class NameFormatter {
    private NameFormatter() { }

    /** The LuckPerms instance. */
    private static final LuckPerms LUCKPERMS = LuckPermsProvider.get();

    static {
        LUCKPERMS.getEventBus().subscribe(DogCorePlugin.getInstance(), GroupDataRecalculateEvent.class, event -> {
            var futures = Bukkit.getOnlinePlayers()
                .stream()
                .map(player -> refreshPlayerName(player).toCompletableFuture())
                .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futures).join();
        });
    }

    private static @NotNull TextColor parseColor(@NotNull final String color) {
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

    private static @NotNull CompletableFuture<@NotNull User> getOrLoadUser(
        @NotNull final UUID uuid,
        @NotNull final String name
    ) {
        var userManager = LUCKPERMS.getUserManager();
        var loadedUser = userManager.getUser(uuid);
        if (loadedUser == null) {
            return userManager.loadUser(uuid, name);
        }
        return CompletableFuture.completedFuture(loadedUser);
    }

    public static @NotNull CompletableFuture<@NotNull Component> formatUsername(
        @NotNull final UUID uuid,
        @NotNull final String username,
        @Nullable final String nickname
    ) {
        return getOrLoadUser(uuid, username).thenApply(user -> {
            String name;
            if (nickname == null) {
                name = username;
            } else {
                name = nickname;
            }
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
            return Component.text(text, color)
                .insertion(username)
                .hoverEvent(HoverEvent.showText(Component.text(username)));
        });
    }

    private static @NotNull CompletionStage<@NotNull Component> formatUsername(@NotNull final Player player) {
        return net.dumbdogdiner.dogcore.database.User.lookup(player).thenCompose(user -> {
            if (user != null) {
                return user.formattedName();
            } else {
                return formatUsername(player.getUniqueId(), player.getName(), null);
            }
        });
    }

    public static @NotNull CompletionStage<Void> refreshPlayerName(@NotNull final Player player) {
        return formatUsername(player).thenAccept(name -> {
            player.displayName(name);
            Bukkit.getPluginManager().callEvent(new DisplayNameChangeEvent(player));
        });
    }
}
