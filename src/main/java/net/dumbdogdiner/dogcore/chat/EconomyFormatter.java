package net.dumbdogdiner.dogcore.chat;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public final class EconomyFormatter {
    private EconomyFormatter() { }

    public static @NotNull Component formatCurrency(final long amount) {
        return Component.text(String.format("%,d %s", amount, amount == 1 ? "bean" : "beans"));
    }
}
