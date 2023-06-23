package net.dumbdogdiner.dogcore;

import com.google.common.base.Preconditions;
import dev.jorel.commandapi.CommandAPI;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;
import net.dumbdogdiner.dogcore.afk.AfkManager;
import net.dumbdogdiner.dogcore.chat.DeathMessageRandomizer;
import net.dumbdogdiner.dogcore.chat.NameFormatter;
import net.dumbdogdiner.dogcore.database.Database;
import net.dumbdogdiner.dogcore.listener.CoreListener;
import net.dumbdogdiner.dogcore.listener.PlayerListNameListener;
import net.dumbdogdiner.dogcore.listener.TabListManager;
import net.dumbdogdiner.dogcore.teleport.BackManager;
import net.dumbdogdiner.dogcore.teleport.SpawnListener;
import net.dumbdogdiner.dogcore.teleport.TeleportHelper;
import net.dumbdogdiner.dogcore.teleport.TpaManager;
import net.dumbdogdiner.dogcore.vault.DogEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class DogCorePlugin extends JavaPlugin {
    /** The instance of this plugin. */
    private static DogCorePlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        TeleportHelper.initSafeTeleport(this);
        Database.init();
        DeathMessageRandomizer.init(this);
        BackManager.init(this);
        TpaManager.init(this);
        TabListManager.init();
        PlayerListNameListener.init();

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

        getServer().getPluginManager().registerEvents(new CoreListener(), this);
        getServer().getPluginManager().registerEvents(new SpawnListener(), this);

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
}
