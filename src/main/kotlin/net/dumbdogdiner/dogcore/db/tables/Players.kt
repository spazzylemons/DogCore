package net.dumbdogdiner.dogcore.db.tables

import org.jetbrains.exposed.sql.Table

object Players : Table() {
    /** The player's unique ID. */
    val uniqueId = uuid("unique_id")

    override val primaryKey = PrimaryKey(uniqueId)
}
