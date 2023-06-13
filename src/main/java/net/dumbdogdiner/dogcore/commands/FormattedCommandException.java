package net.dumbdogdiner.dogcore.commands;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public final class FormattedCommandException extends RuntimeException {
    private final @NotNull Component msg;

    public FormattedCommandException(@NotNull Component msg) {
        this.msg = msg;
    }

    public @NotNull Component getMsg() {
        return msg;
    }
}
