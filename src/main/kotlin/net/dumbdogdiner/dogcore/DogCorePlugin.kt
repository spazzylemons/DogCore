package net.dumbdogdiner.dogcore

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.dumbdogdiner.dogcore.chat.NameFormatter
import net.dumbdogdiner.dogcore.commands.BackCommand
import net.dumbdogdiner.dogcore.commands.EconomyCommands
import net.dumbdogdiner.dogcore.commands.FormattedCommandException
import net.dumbdogdiner.dogcore.commands.MuteCommands
import net.dumbdogdiner.dogcore.commands.NickCommand
import net.dumbdogdiner.dogcore.commands.SnoopCommands
import net.dumbdogdiner.dogcore.commands.TellCommand
import net.dumbdogdiner.dogcore.commands.TpaCommands
import net.dumbdogdiner.dogcore.database.Database
import net.dumbdogdiner.dogcore.listener.CoreListener
import net.dumbdogdiner.dogcore.teleport.initSafeTeleport
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.bukkit.BukkitCommandHandler
import revxrsal.commands.bukkit.sender
import java.time.Duration
import java.time.format.DateTimeParseException

private val COMMANDS_TO_REMOVE = arrayOf("msg")

class DogCorePlugin : JavaPlugin() {
    override fun onEnable() {
        initSafeTeleport(this)
        Database.init(this)

        val handler = BukkitCommandHandler.create(this)

        // duration parser
        val durationException = DynamicCommandExceptionType { LiteralMessage("Failed to parse duration '$it'") }
        handler.registerValueResolver(Duration::class.java) {
            val arg = it.pop()
            try {
                Duration.parse(arg)
            } catch (e: DateTimeParseException) {
                throw durationException.create(arg)
            }
        }

        handler.register(
            BackCommand,
            EconomyCommands,
            MuteCommands,
            NickCommand,
            SnoopCommands,
            TellCommand,
            TpaCommands
        )

        handler.registerExceptionHandler(FormattedCommandException::class.java) { actor, e ->
            actor.sender.sendMessage(e.msg)
        }

        handler.registerBrigadier()

        NameFormatter.init(this)

        server.pluginManager.registerEvents(CoreListener(this), this)

        removeVanillaOverrides(Bukkit.getConsoleSender())

        logger.info("doggy time")
    }

    fun getConfigString(key: String) =
        config.getString(key) ?: throw RuntimeException("missing value for $key in config.yml")

    /**
     * Some Vanilla commands are disabled, to force using our implementation.
     * This modifies the permissions of a command sender to remove those commands.
     */
    fun removeVanillaOverrides(sender: CommandSender) {
        val attachment = sender.addAttachment(this)
        for (cmd in COMMANDS_TO_REMOVE) {
            attachment.setPermission("minecraft.command.$cmd", false)
        }
    }
}
