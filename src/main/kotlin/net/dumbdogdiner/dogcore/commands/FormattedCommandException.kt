package net.dumbdogdiner.dogcore.commands

import net.kyori.adventure.text.Component

class FormattedCommandException(val msg: Component) : RuntimeException()

fun commandError(msg: Component): Nothing = throw FormattedCommandException(msg)
