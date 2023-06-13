package net.dumbdogdiner.dogcore.commands

import net.dumbdogdiner.dogcore.Permissions
import net.dumbdogdiner.dogcore.messages.Messages
import net.dumbdogdiner.dogcore.teleport.safeTeleport
import org.bukkit.Location
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.bukkit.annotation.CommandPermission
import java.util.UUID

object BackCommand {
    private val deathLocations = mutableMapOf<UUID, Location>()

    @Command("back")
    @CommandPermission(Permissions.BACK)
    fun back(player: Player) {
        val location = deathLocations.remove(player.uniqueId)
        if (location == null) {
            player.sendMessage(Messages["commands.back.nothing"])
        } else {
            player.sendMessage(Messages["commands.back.success"])
            safeTeleport(player, location)
        }
    }

    fun setBack(uuid: UUID, location: Location) {
        deathLocations[uuid] = location
    }

    fun removeBack(uuid: UUID) {
        deathLocations.remove(uuid)
    }
}
