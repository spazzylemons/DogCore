package net.dumbdogdiner.dogcore;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.dumbdogdiner.dogcore.afk.AfkManager;
import net.dumbdogdiner.dogcore.chat.NameFormatter;
import net.dumbdogdiner.dogcore.commands.AfkCommand;
import net.dumbdogdiner.dogcore.commands.BackCommand;
import net.dumbdogdiner.dogcore.commands.EconomyCommands;
import net.dumbdogdiner.dogcore.commands.FormattedCommandException;
import net.dumbdogdiner.dogcore.commands.GuiCommands;
import net.dumbdogdiner.dogcore.commands.MuteCommands;
import net.dumbdogdiner.dogcore.commands.NickCommand;
import net.dumbdogdiner.dogcore.commands.SnoopCommands;
import net.dumbdogdiner.dogcore.commands.TellCommand;
import net.dumbdogdiner.dogcore.commands.TpaCommands;
import net.dumbdogdiner.dogcore.database.Database;
import net.dumbdogdiner.dogcore.listener.CoreListener;
import net.dumbdogdiner.dogcore.teleport.SafeTeleport;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import java.time.Duration;
import java.time.format.DateTimeParseException;

public final class DogCorePlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        SafeTeleport.initSafeTeleport(this);
        Database.init(this);

        var handler = BukkitCommandHandler.create(this);

        // duration parser
        var durationException = new DynamicCommandExceptionType(d -> new LiteralMessage("Failed to parse duration " + d));
        handler.registerValueResolver(Duration.class, ctx -> {
            var arg = ctx.pop();
            try {
                return Duration.parse(arg);
            } catch (DateTimeParseException e) {
                throw durationException.create(arg);
            }
        });

        AfkManager.init(this);

        handler.register(
            AfkCommand.class,
            BackCommand.class,
            EconomyCommands.class,
            GuiCommands.class,
            MuteCommands.class,
            NickCommand.class,
            SnoopCommands.class,
            TellCommand.class,
            TpaCommands.class
        );

        handler.registerExceptionHandler(FormattedCommandException.class, (actor, e) -> {
            ((BukkitCommandActor) actor).getSender().sendMessage(e.getMsg());
        });

        handler.registerBrigadier();

        NameFormatter.init(this);

        getServer().getPluginManager().registerEvents(new CoreListener(this), this);

        removeVanillaOverrides(Bukkit.getConsoleSender());

        getLogger().info("doggy time");
    }

    public @NotNull String getConfigString(@NotNull String key) {
        var result = getConfig().getString(key);
        if (result == null) {
            throw new RuntimeException("missing value for " + key + " in config.yml");
        }
        return result;
    }

    /**
     * Some Vanilla commands are disabled, to force using our implementation.
     * This modifies the permissions of a command sender to remove those commands.
     */
    public void removeVanillaOverrides(CommandSender sender) {
        var attachment = sender.addAttachment(this);
        attachment.setPermission("minecraft.command.msg", false);
    }
}
