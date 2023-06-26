package net.dumbdogdiner.dogcore.commands.economy;

import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.arguments.AIntegerArgument;
import net.dumbdogdiner.dogcore.Permissions;
import net.dumbdogdiner.dogcore.chat.MiscFormatter;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Command("baltop")
@Alias("balancetop")
@Permission(Permissions.ECO)
public final class BalanceTopCommand {
    private BalanceTopCommand() { }

    @Default
    public static void balTop(final @NotNull CommandSender sender) {
        balTop(sender, 1);
    }

    @Default
    public static void balTop(
        final @NotNull CommandSender sender,
        final @AIntegerArgument(min = 1) int page
    ) {
        User.top(page).thenAccept(entries -> {
            if (entries.size() == 0) {
                sender.sendMessage(Messages.get("commands.balancetop.end"));
                return;
            }

            var next = Messages.get("commands.balancetop.next")
                .clickEvent(ClickEvent.runCommand("/baltop " + (page + 1)));
            var main = Messages.get("commands.balancetop.page", Component.text(page));
            Component message;
            if (page > 1) {
                var prev = Messages.get("commands.balancetop.prev")
                    .clickEvent(ClickEvent.runCommand("/baltop " + (page - 1)));
                message = Component.textOfChildren(prev, Component.space(), main, Component.space(), next);
            } else {
                message = Component.textOfChildren(main, Component.space(), next);
            }
            sender.sendMessage(message);
            for (var entry : entries) {
                sender.sendMessage(
                    Messages.get(
                        "commands.balancetop.entry",
                        Component.text(entry.index()),
                        entry.name(),
                        MiscFormatter.formatCurrency(entry.amount())
                    ));
            }
        });
    }
}
