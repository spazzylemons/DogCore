package net.dumbdogdiner.dogcore.commands

import net.dumbdogdiner.dogcore.Permissions
import net.dumbdogdiner.dogcore.db.User
import net.dumbdogdiner.dogcore.messages.Messages
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.bukkit.annotation.CommandPermission

object SnoopCommands {

    @Command("invsee")
    @CommandPermission(Permissions.SNOOP)
    fun invSee(sender: Player, player: Player) {
        // TODO we could look into offline player support
        // TODO does not show armor slots/offhand
        sender.openInventory(player.inventory)
    }

    private fun socialSpyHelper(sender: Player, state: Boolean?) {
        val user = User.lookupCommand(sender)
        if (state != null) {
            user.socialSpy = state
        }
        val newState = if (user.socialSpy) "on" else "off"
        sender.sendMessage(Messages["commands.socialspy.check", Component.text(newState)])
    }

    @Command("socialspy check")
    @CommandPermission(Permissions.SNOOP)
    fun socialSpy(sender: Player) {
        socialSpyHelper(sender, null)
    }

    @Command("socialspy off")
    @CommandPermission(Permissions.SNOOP)
    fun socialSpyOff(sender: Player) {
        socialSpyHelper(sender, false)
    }

    @Command("socialspy on")
    @CommandPermission(Permissions.SNOOP)
    fun socialSpyOn(sender: Player) {
        socialSpyHelper(sender, true)
    }
}
