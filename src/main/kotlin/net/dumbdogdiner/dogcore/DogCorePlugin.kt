package net.dumbdogdiner.dogcore

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.dumbdogdiner.dogcore.chat.NameFormatter
import net.dumbdogdiner.dogcore.commands.EconomyCommands
import net.dumbdogdiner.dogcore.commands.MuteCommands
import net.dumbdogdiner.dogcore.commands.SnoopCommands
import net.dumbdogdiner.dogcore.commands.TellCommand
import net.dumbdogdiner.dogcore.db.Db
import net.dumbdogdiner.dogcore.listener.CoreListener
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.bukkit.BukkitCommandHandler
import kotlin.time.Duration

private val COMMANDS_TO_REMOVE = arrayOf("msg")

class DogCorePlugin : JavaPlugin() {
    override fun onEnable() {
        Db.init(this)

        val handler = BukkitCommandHandler.create(this)

        // duration parser
        val durationException = DynamicCommandExceptionType { LiteralMessage("Failed to parse duration '$it'") }
        handler.registerValueResolver(Duration::class.java) {
            val arg = it.pop()
            Duration.parseOrNull(arg) ?: throw durationException.create(arg)
        }

        handler.register(
            EconomyCommands,
            MuteCommands,
            SnoopCommands,
            TellCommand
        )

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
