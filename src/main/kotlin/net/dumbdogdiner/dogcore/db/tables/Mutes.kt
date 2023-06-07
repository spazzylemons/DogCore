package net.dumbdogdiner.dogcore.db.tables

import org.jetbrains.exposed.sql.Table

object Mutes : Table() {
    /** The muted player. */
    val playerId = uuid("player_id").references(Players.uniqueId)

    /** The time when the mute expires. If null, mute is indefinite. */
    val expires = long("expires").nullable()

    override val primaryKey = PrimaryKey(playerId)
}
