package net.dumbdogdiner.dogcore.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.dumbdogdiner.dogcore.DogCorePlugin;
import org.jetbrains.annotations.NotNull;

public final class Configuration {
    private Configuration() { }

    /** The set of functions to run when the configuration is reloaded. */
    private static final Set<Runnable> REGISTRATIONS = new HashSet<>();

    public static void load() {
        DogCorePlugin.getInstance().reloadConfig();
        for (var cfg : REGISTRATIONS) {
            cfg.run();
        }
    }

    public static void register(final @NotNull Runnable r) {
        REGISTRATIONS.add(r);
        r.run();
    }

    public static @NotNull String getString(final @NotNull String path) {
        var result = DogCorePlugin.getInstance().getConfig().getString(path);
        if (result == null) {
            throw missingConfig(path);
        }
        return result;
    }

    public static @NotNull List<@NotNull String> getStrings(final @NotNull String path) {
        return DogCorePlugin.getInstance().getConfig().getStringList(path);
    }

    public static int getInt(final @NotNull String path) {
        var result = DogCorePlugin.getInstance().getConfig().get(path);
        if (!(result instanceof Number num)) {
            throw missingConfig(path);
        }
        return num.intValue();
    }

    private static RuntimeException missingConfig(final @NotNull String path) {
        return new RuntimeException("Missing data for " + path + " in config.yml.");
    }
}
