package net.dumbdogdiner.dogcore.commands;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public final class SnoopCommands {
    private SnoopCommands() {}

    @Command("invsee")
    @CommandPermission(Permissions.SNOOP)
    public static void invSee(Player sender, Player player) {
        // TODO we could look into offline player support
        // TODO does not show armor slots/offhand
        sender.openInventory(player.getInventory());
    }

    private static void socialSpyHelper(Player sender, @Nullable Boolean state) {
        User.lookupCommand(sender, sender).thenAccept(user -> {
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

    @Command("socialspy check")
    @CommandPermission(Permissions.SNOOP)
    public static void socialSpy(Player sender) {
        socialSpyHelper(sender, null);
    }

    @Command("socialspy off")
    @CommandPermission(Permissions.SNOOP)
    public static void socialSpyOff(Player sender) {
        socialSpyHelper(sender, false);
    }

    @Command("socialspy on")
    @CommandPermission(Permissions.SNOOP)
    public static void socialSpyOn(Player sender) {
        socialSpyHelper(sender, true);
    }
}
