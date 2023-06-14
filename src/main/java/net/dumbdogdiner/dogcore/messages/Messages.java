package net.dumbdogdiner.dogcore.messages;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;

public final class Messages {
    private Messages() {}

    private static final ResourceBundle bundle = ResourceBundle.getBundle("messages");

    public static @NotNull Component get(@NotNull String key, @NotNull Component... args) {
        String message;
        try {
            message = bundle.getString(key);
        } catch (MissingResourceException e) {
            return Component.text(key);
        }

        var builder = TagResolver.builder().resolver(StandardTags.defaults());
        for (var i = 0; i < args.length; i++) {
            builder.resolver(Placeholder.component(String.valueOf(i), args[i]));
        }

        return MiniMessage.builder()
            .tags(builder.build())
            .build()
            .deserialize(message);
    }
}
