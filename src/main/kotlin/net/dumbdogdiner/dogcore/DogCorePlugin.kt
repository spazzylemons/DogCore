package net.dumbdogdiner.dogcore

import net.dumbdogdiner.dogcore.commands.muteCommand
import net.dumbdogdiner.dogcore.commands.unmuteCommand
import net.dumbdogdiner.dogcore.db.Db
import net.dumbdogdiner.dogcore.listener.ChatFormatter
import org.bukkit.plugin.java.JavaPlugin

class DogCorePlugin : JavaPlugin() {
    override fun onEnable() {
        INSTANCE = this

        Db.init()

        muteCommand()
        unmuteCommand()

        server.pluginManager.registerEvents(ChatFormatter, this)
        logger.info("doggy time")
    }

    fun getConfigString(key: String) =
        config.getString(key) ?: throw RuntimeException("missing value for $key in config.yml")

    companion object {
        lateinit var INSTANCE: DogCorePlugin
    }
}
