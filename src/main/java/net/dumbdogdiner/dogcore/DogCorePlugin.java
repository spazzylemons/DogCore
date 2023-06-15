package net.dumbdogdiner.dogcore;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.dumbdogdiner.dogcore.afk.AfkManager;
import net.dumbdogdiner.dogcore.chat.DeathMessageRandomizer;
import net.dumbdogdiner.dogcore.chat.NameFormatter;
import net.dumbdogdiner.dogcore.commands.AfkCommand;
import net.dumbdogdiner.dogcore.commands.BackCommand;
import net.dumbdogdiner.dogcore.commands.EconomyCommands;
import net.dumbdogdiner.dogcore.commands.GuiCommands;
import net.dumbdogdiner.dogcore.commands.MuteCommands;
import net.dumbdogdiner.dogcore.commands.NickCommand;
import net.dumbdogdiner.dogcore.commands.SnoopCommands;
import net.dumbdogdiner.dogcore.commands.TellCommand;
import net.dumbdogdiner.dogcore.commands.TpaCommands;
import net.dumbdogdiner.dogcore.database.Database;
import net.dumbdogdiner.dogcore.listener.CoreListener;
import net.dumbdogdiner.dogcore.teleport.BackManager;
import net.dumbdogdiner.dogcore.teleport.SafeTeleport;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import java.time.Duration;
import java.time.format.DateTimeParseException;

public final class DogCorePlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        SafeTeleport.initSafeTeleport(this);
        Database.init(this);
        DeathMessageRandomizer.init(this);
        BackManager.init(this);

        var handler = BukkitCommandHandler.create(this);

        // duration parser
        var durationException = new DynamicCommandExceptionType(d ->
            new LiteralMessage("Failed to parse duration " + d));
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

        handler.registerBrigadier();

        NameFormatter.init(this);

        getServer().getPluginManager().registerEvents(new CoreListener(this), this);

        removeVanillaOverrides(Bukkit.getConsoleSender());

        getLogger().info("doggy time");
    }

    /**
     * Get a string from the configuration file.
     * @param key The path to search.
     * @return The data in the configuration file.
     */
    public @NotNull String getConfigString(@NotNull final String key) {
        var result = getConfig().getString(key);
        if (result == null) {
            throw new RuntimeException("missing value for " + key + " in config.yml");
        }
        return result;
    }

    /**
     * Some Vanilla commands are disabled, to force using our implementation.
     * This modifies the permissions of a command sender to remove them.
     * @param sender The sender to remove the permissions from.
     */
    public void removeVanillaOverrides(@NotNull final CommandSender sender) {
        var attachment = sender.addAttachment(this);
        attachment.setPermission("minecraft.command.msg", false);
    }
}
