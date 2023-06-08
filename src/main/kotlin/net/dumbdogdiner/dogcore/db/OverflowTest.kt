package net.dumbdogdiner.dogcore.db

fun Long.addWithoutOverflow(other: Long) =
    if (this > 0L && other > Long.MAX_VALUE - this) {
        null
    } else if (this < 0L && other < Long.MIN_VALUE - this) {
        null
    } else {
        this + other
    }
