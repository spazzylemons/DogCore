package net.dumbdogdiner.dogcore.chat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import net.dumbdogdiner.dogcore.DogCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class MiscFormatter {
    private MiscFormatter() { }

    /** A join configuration that uses commas, and "and" for the last separator. */
    private static final JoinConfiguration JOIN_CONFIG =
        JoinConfiguration.separators(Component.text(", "), Component.text(" and "));

    public static @NotNull Component formatCurrency(final long amount) {
        String name;
        if (amount == 1L) {
            name = DogCorePlugin.getEconomy().currencyNameSingular();
        } else {
            name = DogCorePlugin.getEconomy().currencyNamePlural();
        }
        return Component.text(String.format("%,d %s", amount, name));
    }

    private static @NotNull String durationAmount(final long i, final @NotNull String v) {
        return String.format("%d %s", i, i == 1 ? v : v + "s");
    }

    /** Divider 1. */
    private static final int DIVIDER_1 = 60;

    /** Divider 2. */
    private static final int DIVIDER_2 = 24;

    /** Divider 3. */
    private static final int DIVIDER_3 = 7;

    public static @NotNull Component formatDuration(final @NotNull Duration duration) {
        var seconds = duration.toSeconds();
        // remove minutes
        var minutes = seconds / DIVIDER_1;
        seconds %= DIVIDER_1;
        // remove hours
        var hours = minutes / DIVIDER_1;
        minutes %= DIVIDER_1;
        // remove days
        var days = hours / DIVIDER_2;
        hours %= DIVIDER_2;
        // remove weeks
        var weeks = days / DIVIDER_3;
        days %= DIVIDER_3;
        // build string from components
        var result = new ArrayList<String>();
        if (weeks != 0) {
            result.add(durationAmount(weeks, "week"));
        }
        if (days != 0) {
            result.add(durationAmount(days, "day"));
        }
        if (hours != 0) {
            result.add(durationAmount(hours, "hour"));
        }
        if (minutes != 0) {
            result.add(durationAmount(minutes, "minute"));
        }
        if (seconds != 0) {
            result.add(durationAmount(seconds, "second"));
        }
        if (result.size() > 1) {
            for (var i = 0; i < result.size(); i++) {
                if (i == result.size() - 1) {
                    result.set(i, " and " + result.get(i));
                } else if (i > 0) {
                    result.set(i, ", " + result.get(i));
                }
            }
        }
        return Component.text(String.join("", result.toArray(String[]::new)));
    }

    public static @NotNull Component playerCollection(final @NotNull Collection<Player> players) {
        return Component.join(JOIN_CONFIG, players.stream().map(Player::displayName).toArray(Component[]::new));
    }
}
