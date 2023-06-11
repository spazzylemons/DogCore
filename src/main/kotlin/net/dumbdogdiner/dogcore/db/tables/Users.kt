package net.dumbdogdiner.dogcore.db.tables

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    /** The player's unique ID. */
    val uniqueId = uuid("unique_id")

    /**
     * The last seen username of this player.
     * Almost always unique, but not guaranteed.
     */
    val username = varchar("username", 16)

    /** The amount of currency owned by this player. */
    val balance = long("balance").default(0)

    /** If true, this user has social spy enabled and can see private messages. */
    val socialSpy = bool("social_spy").default(false)

    override val primaryKey = PrimaryKey(uniqueId)
}
