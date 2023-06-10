package net.dumbdogdiner.dogcore.commands

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import dev.jorel.commandapi.kotlindsl.offlinePlayerArgument
import net.dumbdogdiner.dogcore.Permissions
import net.dumbdogdiner.dogcore.db.DbPlayer
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import kotlin.time.Duration

fun muteCommand() = commandAPICommand("mute") {
    withPermission(Permissions.MUTE)

    offlinePlayerArgument("player")
    greedyStringArgument("duration", optional = true)

    anyExecutor { sender, args ->
        val player = DbPlayer.lookup(args["player"] as OfflinePlayer)
        if (player == null) {
            sender.sendMessage(Component.text("Player has no records in the server."))
            return@anyExecutor
        }

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
