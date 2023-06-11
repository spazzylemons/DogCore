package net.dumbdogdiner.dogcore.commands

import net.dumbdogdiner.dogcore.Permissions
import net.dumbdogdiner.dogcore.db.DbPlayer
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Optional
import revxrsal.commands.bukkit.annotation.CommandPermission
import kotlin.time.Duration

object MuteCommands {
    @Command("mute")
    @CommandPermission(Permissions.MUTE)
    fun mute(
        sender: CommandSender,
        player: OfflinePlayer,
        @Optional duration: Duration?
    ) {
        val p = DbPlayer.lookup(player)
        if (p == null) {
            sender.sendMessage(Component.text("Player has no records in the server."))
            return
        }
        p.mute(duration)
    }

    @Command("unmute")
    @CommandPermission(Permissions.MUTE)
    fun unmute(sender: CommandSender, player: OfflinePlayer) {
        val p = DbPlayer.lookup(player)
        if (p == null) {
            sender.sendMessage(Component.text("Player has no records in the server."))
            return
        }
        p.unmute()
    }
}
