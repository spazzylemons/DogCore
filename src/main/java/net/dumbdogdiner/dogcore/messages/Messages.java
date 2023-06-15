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
    private Messages() { }

    /**
     * The properties file containing the messages.
     */
    private static final ResourceBundle BUNDLE =
        ResourceBundle.getBundle("messages");

    /**
     * Format a message.
     * @param key The key of the message.
     * @param args The components to insert into the message.
     * @return A formatted message.
     */
    public static @NotNull Component get(
        @NotNull final String key,
        @NotNull final Component... args
    ) {
        String message;
        try {
            message = BUNDLE.getString(key);
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
