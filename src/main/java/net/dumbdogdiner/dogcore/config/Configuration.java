package net.dumbdogdiner.dogcore.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.dumbdogdiner.dogcore.DogCorePlugin;
import org.jetbrains.annotations.NotNull;

public final class Configuration {
    private Configuration() { }

    /** The set of configurables. */
    private static final Set<Configurable> CONFIGURABLES = new HashSet<>();

    public static void load() {
        DogCorePlugin.getInstance().reloadConfig();
        for (var cfg : CONFIGURABLES) {
            cfg.loadConfig();
        }
    }

    public static void register(final @NotNull Configurable configurable) {
        CONFIGURABLES.add(configurable);
        configurable.loadConfig();
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
