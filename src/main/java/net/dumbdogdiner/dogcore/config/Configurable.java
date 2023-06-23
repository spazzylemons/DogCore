package net.dumbdogdiner.dogcore.config;

/**
 * Represents a system that can be configured.
 */
@FunctionalInterface
public interface Configurable {
    void loadConfig();
}
