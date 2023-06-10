package net.dumbdogdiner.dogcore

import net.dumbdogdiner.dogcore.chat.NameFormatter
import net.dumbdogdiner.dogcore.commands.balCommand
import net.dumbdogdiner.dogcore.commands.balTopCommand
import net.dumbdogdiner.dogcore.commands.ecoCommand
import net.dumbdogdiner.dogcore.commands.muteCommand
import net.dumbdogdiner.dogcore.commands.payCommand
import net.dumbdogdiner.dogcore.commands.unmuteCommand
import net.dumbdogdiner.dogcore.db.Db
import net.dumbdogdiner.dogcore.listener.CoreListener
import org.bukkit.plugin.java.JavaPlugin

class DogCorePlugin : JavaPlugin() {
    override fun onEnable() {
        Db.init(this)

        balCommand()
        balTopCommand()
        ecoCommand()
        muteCommand()
        payCommand()
        unmuteCommand()

        NameFormatter.init(this)

        server.pluginManager.registerEvents(CoreListener(this), this)

        logger.info("doggy time")
    }

    fun getConfigString(key: String) =
        config.getString(key) ?: throw RuntimeException("missing value for $key in config.yml")
}
