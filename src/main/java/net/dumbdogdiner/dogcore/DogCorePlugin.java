package net.dumbdogdiner.dogcore;

import dev.jorel.commandapi.CommandAPI;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;
import net.dumbdogdiner.dogcore.afk.AfkManager;
import net.dumbdogdiner.dogcore.chat.DeathMessageRandomizer;
import net.dumbdogdiner.dogcore.chat.NameFormatter;
import net.dumbdogdiner.dogcore.database.Database;
import net.dumbdogdiner.dogcore.listener.CoreListener;
import net.dumbdogdiner.dogcore.teleport.BackManager;
import net.dumbdogdiner.dogcore.teleport.SpawnListener;
import net.dumbdogdiner.dogcore.teleport.TeleportHelper;
import net.dumbdogdiner.dogcore.teleport.TpaManager;
import net.dumbdogdiner.dogcore.vault.DogEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class DogCorePlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        TeleportHelper.initSafeTeleport(this);
        Database.init(this);
        DeathMessageRandomizer.init(this);
        BackManager.init(this);
        TpaManager.init(this);

        Bukkit.getServicesManager().register(Economy.class, new DogEconomy(), this, ServicePriority.Highest);

        // goodbye, /tell!
        CommandAPI.unregister("tell");

        try (var stream = new FileInputStream(getFile())) {
            try (var zip = new ZipInputStream(stream)) {
                for (;;) {
                    var entry = zip.getNextEntry();
                    if (entry == null) {
                        break;
                    }

                    var name = entry.getName();
                    if (name.startsWith("net/dumbdogdiner/dogcore/commands/")
                        && name.endsWith(".class")
                        && !name.contains("$")) {
                        var className = name.substring(0, name.indexOf(".")).replaceAll("/", ".");
                        var clazz = getClassLoader().loadClass(className);
                        CommandAPI.registerCommand(clazz);
                    }
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }

        AfkManager.init(this);

        NameFormatter.init(this);

        getServer().getPluginManager().registerEvents(new CoreListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnListener(), this);

        removeVanillaOverrides(Bukkit.getConsoleSender());

        getLogger().info("doggy time");
    }

    /**
     * Get a string from the configuration file.
     * @param key The path to search.
     * @return The data in the configuration file.
     */
    public @NotNull String getConfigString(@NotNull final String key) {
        var result = getConfig().getString(key);
        if (result == null) {
            throw new RuntimeException("missing value for " + key + " in config.yml");
        }
        return result;
    }

    /**
     * Some Vanilla commands are disabled, to force using our implementation.
     * This modifies the permissions of a command sender to remove them.
     * @param sender The sender to remove the permissions from.
     */
    public void removeVanillaOverrides(@NotNull final CommandSender sender) {
//        var attachment = sender.addAttachment(this);
//        attachment.setPermission("minecraft.command.msg", false);
    }

    /**
     * Create a {@link org.bukkit.NamespacedKey}.
     * @param key The key.
     * @return The namespaced key.
     */
    public static @NotNull NamespacedKey key(@NotNull final String key) {
        return new NamespacedKey("dogcore", key);
    }
}
