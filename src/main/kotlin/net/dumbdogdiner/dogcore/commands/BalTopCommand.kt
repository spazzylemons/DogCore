package net.dumbdogdiner.dogcore.commands

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.integerArgument
import net.dumbdogdiner.dogcore.db.DbPlayer
import net.kyori.adventure.text.Component

fun balTopCommand() = commandAPICommand("baltop") {
    withAliases("balancetop")
    integerArgument("page", optional = true, min = 1)

    anyExecutor { sender, args ->
        val page = (args["page"] as Int?) ?: 1
        for ((username, balance) in DbPlayer.top(page)) {
            sender.sendMessage(Component.text("$username -> $balance"))
        }
    }
}
