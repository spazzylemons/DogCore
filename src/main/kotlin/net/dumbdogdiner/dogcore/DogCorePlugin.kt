package net.dumbdogdiner.dogcore

import net.dumbdogdiner.dogcore.chat.PrefixManager
import net.dumbdogdiner.dogcore.commands.balCommand
import net.dumbdogdiner.dogcore.commands.balTopCommand
import net.dumbdogdiner.dogcore.commands.ecoCommand
import net.dumbdogdiner.dogcore.commands.muteCommand
import net.dumbdogdiner.dogcore.commands.payCommand
import net.dumbdogdiner.dogcore.commands.unmuteCommand
import net.dumbdogdiner.dogcore.db.Db
import net.dumbdogdiner.dogcore.listener.ChatFormatter
import net.dumbdogdiner.dogcore.listener.PlayerRegistrar
import net.dumbdogdiner.dogcore.listener.TabListFormatter
import org.bukkit.plugin.java.JavaPlugin

class DogCorePlugin : JavaPlugin() {
    override fun onEnable() {
        INSTANCE = this

        Db.init()

        balCommand()
        balTopCommand()
        ecoCommand()
        muteCommand()
        payCommand()
        unmuteCommand()

        PrefixManager.registerEvents(this)

        server.pluginManager.registerEvents(ChatFormatter, this)
        server.pluginManager.registerEvents(PlayerRegistrar, this)
        server.pluginManager.registerEvents(TabListFormatter, this)

        logger.info("doggy time")
    }

    fun getConfigString(key: String) =
        config.getString(key) ?: throw RuntimeException("missing value for $key in config.yml")

    companion object {
        lateinit var INSTANCE: DogCorePlugin
    }
}
