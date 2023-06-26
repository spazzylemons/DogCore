package net.dumbdogdiner.dogcore.listener;

import net.dumbdogdiner.dogcore.config.Configurable;
import net.dumbdogdiner.dogcore.config.Configuration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.jetbrains.annotations.NotNull;

public final class MOTDListener implements Listener, Configurable {
    /** The formatter. */
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
        .tags(TagResolver.standard())
        .build();

    /** The first line. */
    private Component line1;

    /** The options for the second line. */
    private Component[] line2;

    public MOTDListener() {
        Configuration.register(this);
    }

    @EventHandler
    public void onServerPing(final @NotNull ServerListPingEvent event) {
        Component selectedLine2;
        if (line2.length == 0) {
            selectedLine2 = Component.empty();
        } else {
            selectedLine2 = line2[(int) (Math.random() * line2.length)];
        }
        event.motd(Component.textOfChildren(line1, Component.newline(), selectedLine2));
    }

    @Override
    public void loadConfig() {
        var l1 = MINI_MESSAGE.deserialize(Configuration.getString("motd.line1"));
        var l2 = Configuration.getStrings("motd.line2")
            .stream()
            .map(MINI_MESSAGE::deserialize)
            .toArray(Component[]::new);
        line1 = l1;
        line2 = l2;
    }
}
