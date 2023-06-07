package net.dumbdogdiner.dogcore.db

import net.dumbdogdiner.dogcore.db.tables.Mutes
import net.dumbdogdiner.dogcore.db.tables.Players
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.util.UUID
import kotlin.time.Duration

class DbPlayer(private val uuid: UUID) {
    init {
        Db.transaction {
            // ensure an entry exists for us in the players DB before continuing
            if (Players.select { Players.uniqueId eq uuid }.empty()) {
                Players.insert {
                    it[uniqueId] = uuid
                }
            }
        }
    }

    val isMuted
        get() = Db.transaction {
            // does an entry exist in the mutes table?
            val mute = Mutes.select { Mutes.playerId eq uuid }.firstOrNull()
                ?: return@transaction false // there is no mute, so the player is not muted
            // does the mute have an expiration time?
            val expirationTime = mute[Mutes.expires]
                ?: return@transaction true // if the expiration time is null, mute is indefinite
            // has that time passed?
            val now = System.currentTimeMillis()
            if (now >= expirationTime) {
                // the mute has expired, so delete the row
                Mutes.deleteWhere { playerId eq uuid }
                // no longer muted
                return@transaction false
            }
            true
        }

    /**
     * Mute for the given [duration], or indefinitely if duration is null.
     * Replaces any existing mute duration if one exists.
     */
    fun mute(duration: Duration?) {
        val now = System.currentTimeMillis()
        Db.transaction {
            Mutes.upsert({ Mutes.playerId eq uuid }) {
                it[playerId] = uuid
                it[expires] = duration?.let { d -> now + d.inWholeMilliseconds }
            }
        }
    }

    /**
     * Unmute this player.
     */
    fun unmute() {
        Db.transaction {
            Mutes.deleteWhere { playerId eq uuid }
        }
    }
}
