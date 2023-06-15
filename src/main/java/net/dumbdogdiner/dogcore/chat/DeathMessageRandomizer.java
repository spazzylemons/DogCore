package net.dumbdogdiner.dogcore.chat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DeathMessageRandomizer {
    private DeathMessageRandomizer() { }

    /** The messages, grouped by damage cause. */
    private static final Map<EntityDamageEvent.@NotNull DamageCause, @NotNull String @NotNull[]> MESSAGES
        = new HashMap<>();

    /** The random number generator used. */
    private static final Random RANDOM = new Random();

    /** The filename of the death messages resource. */
    private static final String FILE = "death.yml";

    /**
     * Initialize the death message randomizer.
     * @param plugin The plugin to load messages from.
     */
    public static void init(@NotNull final JavaPlugin plugin) {
        var file = new File(plugin.getDataFolder(), FILE);
        if (!file.exists()) {
            plugin.saveResource(FILE, false);
        }

        var config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            plugin.getLogger().warning("Failed to load death messages. No custom death messages will be used.");
        }

        for (var key : config.getKeys(false)) {
            EntityDamageEvent.DamageCause cause;
            try {
                cause = EntityDamageEvent.DamageCause.valueOf(key.toUpperCase().replace('-', '_'));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid damage cause '" + key + "' - ignored");
                continue;
            }
            var options = config.getStringList(key);
            var array = new String[options.size()];
            options.toArray(array);
            MESSAGES.put(cause, array);
        }
    }

    /**
     * Select a random death message.
     * @param cause The cause of the last damage received.
     * @param player The player's name.
     * @param attacker The attacker, if applicable and found.
     * @return A component to display, or null to use default.
     */
    public static @Nullable Component select(
        @NotNull final EntityDamageEvent.DamageCause cause,
        @NotNull final Component player,
        @Nullable final Component attacker
    ) {
        var list = MESSAGES.get(cause);
        if (list == null) {
            return null;
        }
        var choice = list[RANDOM.nextInt(list.length)];
        var builder = TagResolver.builder()
            .resolver(Placeholder.component("player", player));
        if (attacker != null) {
            builder.resolver(Placeholder.component("attacker", attacker));
        }
        return MiniMessage.builder()
            .tags(builder.build())
            .build()
            .deserialize(choice);
    }
}
