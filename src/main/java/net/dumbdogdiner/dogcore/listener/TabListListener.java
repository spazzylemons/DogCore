package net.dumbdogdiner.dogcore.listener;

import net.dumbdogdiner.dogcore.DogCorePlugin;
import net.dumbdogdiner.dogcore.config.Configuration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public final class TabListListener implements Listener {

    /** The format of the header string. */
    private static String headerText;

    /** The format of the footer string. */
    private static String footerText;

    /** Repeat every 10 seconds - update tab list displays. */
    private static final long TASK_PERIOD = 200L;

    static {
        Bukkit.getScheduler().runTaskTimer(DogCorePlugin.getInstance(), () -> {
            synchronized (TabListListener.class) {
                for (var player : Bukkit.getOnlinePlayers()) {
                    sendTabList(player);
                }
            }
        }, 0L, TASK_PERIOD);

        Configuration.register(() -> {
            var header = Configuration.getString("tablist.header");
            var footer = Configuration.getString("tablist.footer");
            synchronized (TabListListener.class) {
                headerText = header;
                footerText = footer;
            }
        });
    }

    private static synchronized void sendTabList(final @NotNull Player player) {
        var miniMessage = MiniMessage.builder()
            .tags(TagResolver.builder()
                .resolver(TagResolver.standard())
                .resolver(Placeholder.component("ms", () -> Component.text(player.getPing())))
                .resolver(Placeholder.component("tps", () -> Component.text(String.format("%.1f", Bukkit.getTPS()[0]))))
                .build())
            .build();

        var header = miniMessage.deserialize(headerText);
        var footer = miniMessage.deserialize(footerText);
        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    @EventHandler
    public void onJoin(final @NotNull PlayerJoinEvent event) {
        sendTabList(event.getPlayer());
    }
}
