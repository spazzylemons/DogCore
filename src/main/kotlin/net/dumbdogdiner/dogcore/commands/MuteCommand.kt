package net.dumbdogdiner.dogcore.commands

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import dev.jorel.commandapi.kotlindsl.playerArgument
import net.dumbdogdiner.dogcore.db.DbPlayer
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import kotlin.time.Duration

fun muteCommand() = commandAPICommand("mute") {
    playerArgument("player")
    greedyStringArgument("duration", optional = true)

    anyExecutor { sender, args ->
        val player = DbPlayer((args["player"] as Player).uniqueId)
        val duration = args["duration"] as String?

        if (duration != null) {
            val parsedDuration = Duration.parseOrNull(duration)
            if (parsedDuration == null) {
                sender.sendMessage(Component.text("Failed to parse mute duration."))
            }
            player.mute(parsedDuration)
        } else {
            player.mute(null)
        }
    }
}
