package net.dumbdogdiner.dogcore;

import com.google.common.base.Preconditions;
import dev.jorel.commandapi.CommandAPI;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;
import net.dumbdogdiner.dogcore.chat.SecureChatSpoofer;
import net.dumbdogdiner.dogcore.vault.DogEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class DogCorePlugin extends JavaPlugin {
    /** The instance of this plugin. */
    private static DogCorePlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        // register our economy implementation
        Bukkit.getServicesManager().register(Economy.class, new DogEconomy(), this, ServicePriority.Highest);
        // remove commands that we replace
        CommandAPI.unregister("tell");
        CommandAPI.unregister("msg");
        CommandAPI.unregister("w");
        CommandAPI.unregister("me");
        // reflection trickery to register commands and listeners
        try (var stream = new FileInputStream(getFile())) {
            try (var zip = new ZipInputStream(stream)) {
                for (;;) {
                    var entry = zip.getNextEntry();
                    if (entry == null) {
                        break;
                    }

                    var name = entry.getName();
                    if (!name.startsWith("net/dumbdogdiner/dogcore/") || !name.endsWith(".class")) {
                        continue;
                    }
                    var className = name.substring(0, name.indexOf(".")).replaceAll("/", ".");
                    if (name.startsWith("net/dumbdogdiner/dogcore/commands/") && !name.contains("$")) {
                        var clazz = getClassLoader().loadClass(className);
                        CommandAPI.registerCommand(clazz);
                    } else if (name.startsWith("net/dumbdogdiner/dogcore/listener/")) {
                        var clazz = getClassLoader().loadClass(className);
                        if (Listener.class.isAssignableFrom(clazz)) {
                            var listener = (Listener) clazz.getConstructor().newInstance();
                            getServer().getPluginManager().registerEvents(listener, this);
                        }
                    }
                }
            }
        } catch (ReflectiveOperationException | IOException e) {
            throw new RuntimeException(e);
        }

        SecureChatSpoofer.register();

        getLogger().info("doggy time");
    }

    public static @NotNull DogCorePlugin getInstance() {
        Preconditions.checkNotNull(instance);
        return instance;
    }

    /**
     * Create a {@link org.bukkit.NamespacedKey}.
     * @param key The key.
     * @return The namespaced key.
     */
    public static @NotNull NamespacedKey key(@NotNull final String key) {
        return new NamespacedKey("dogcore", key);
    }

    public static @NotNull Economy getEconomy() {
        var registration = Bukkit.getServicesManager().getRegistration(Economy.class);
        // we supplied it, so it cannot be null
        assert registration != null;
        return registration.getProvider();
    }
}
