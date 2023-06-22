package net.dumbdogdiner.dogcore.commands.snoop;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.Subcommand;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Command("socialspy")
@Permission(Permissions.SNOOP)
public final class SocialSpyCommand {
    private SocialSpyCommand() { }

    private static void socialSpyHelper(
        @NotNull final Player sender,
        @Nullable final Boolean state
    ) {
        User.lookupCommand(sender, sender, user -> {
            CompletionStage<Void> future;
            if (state != null) {
                future = user.setSocialSpy(state);
            } else {
                future = CompletableFuture.completedFuture(null);
            }
            future.thenApply(v -> user.getSocialSpy().thenAccept(result -> {
                var newState = result ? "on" : "off";
                sender.sendMessage(Messages.get("commands.socialspy.check", Component.text(newState)));
            }));
        });
    }

    @Default
    public static void check(final @NotNull Player sender) {
        socialSpyHelper(sender, null);
    }

    @Subcommand("on")
    public static void on(final @NotNull Player sender) {
        socialSpyHelper(sender, true);
    }

    @Subcommand("off")
    public static void off(final @NotNull Player sender) {
        socialSpyHelper(sender, false);
    }
}
