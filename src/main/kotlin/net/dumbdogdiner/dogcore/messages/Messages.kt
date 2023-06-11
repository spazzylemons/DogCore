package net.dumbdogdiner.dogcore.messages

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import java.util.MissingResourceException
import java.util.ResourceBundle

object Messages {
    private val bundle = ResourceBundle.getBundle("messages")

    operator fun get(id: String, vararg fmt: Component): Component {
        // get the JSON tree
        val mm = try {
            bundle.getString(id)
        } catch (e: MissingResourceException) {
            return Component.text(id)
        }

        val builder = TagResolver.builder().resolver(StandardTags.defaults())
        for ((i, c) in fmt.withIndex()) {
            builder.resolver(Placeholder.component(i.toString(), c))
        }

        return MiniMessage.builder()
            .tags(builder.build())
            .build()
            .deserialize(mm)
    }
}
