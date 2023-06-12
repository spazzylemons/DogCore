package net.dumbdogdiner.dogcore.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.plugin.java.JavaPlugin
import kotlin.random.Random

class DeathMessageRandomizer(plugin: JavaPlugin) {
    private val messages = mutableMapOf<DamageCause, Array<String>>()

    init {
        val file = plugin.dataFolder.resolve(FILE)
        plugin.saveResource(FILE, false)

        val config = YamlConfiguration()
        file.reader().use {
            config.load(it)
        }

        for (key in config.getKeys(false)) {
            val cause = DamageCause.valueOf(key.uppercase().replace('-', '_'))
            messages[cause] = config.getStringList(key).toTypedArray()
        }
    }

    fun select(damageCause: DamageCause, player: Component, entity: Component?, block: Component?): Component? {
        val list = messages[damageCause] ?: return null
        val mm = list[Random.Default.nextInt(list.size)]
        val builder = TagResolver.builder()
            .resolver(Placeholder.component("player", player))
        entity?.let { builder.resolver(Placeholder.component("entity", it)) }
        block?.let { builder.resolver(Placeholder.component("block", it)) }
        return MiniMessage.builder()
            .tags(builder.build())
            .build()
            .deserialize(mm)
    }

    companion object {
        const val FILE = "death.yml"
    }
}
