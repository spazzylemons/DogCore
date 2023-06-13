package net.dumbdogdiner.dogcore.db

import net.dumbdogdiner.dogcore.chat.NameFormatter
import net.dumbdogdiner.dogcore.commands.commandError
import net.dumbdogdiner.dogcore.db.tables.Mutes
import net.dumbdogdiner.dogcore.db.tables.Users
import net.dumbdogdiner.dogcore.messages.Messages
import net.dumbdogdiner.dogcore.util.CoroutineThreadPool
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration

class User private constructor(private val uuid: UUID) {
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
    fun unmute() = Db.transaction {
        Mutes.deleteWhere { playerId eq uuid }
    }

    var balance
        get() = Db.transaction {
            Users.select { Users.uniqueId eq uuid }.first()[Users.balance]
        }
        set(value) = Db.transaction {
            Users.update({ Users.uniqueId eq uuid }) {
                it[balance] = value
            }
        }

    /**
     * Give or take money from this player.
     * Allows entering debt.
     * Fails if balance would overflow.
     */
    fun give(amount: Long) = Db.transaction {
        // How much do we currently have?
        val newBalance = balance.addWithoutOverflow(amount)
        if (newBalance != null) {
            balance = newBalance
            true
        } else {
            false
        }
    }

    /**
     * Pay another player.
     * Does not allow entering debt, but does allow paying someone who is in debt.
     * Fails if there are insufficient funds, or if balance would overflow.
     */
    fun pay(other: User, amount: Long) = Db.transaction {
        // don't allow paying ourselves, or we'd generate infinite money
        if (other.uuid == uuid) return@transaction false
        // don't allow paying negatives
        if (amount < 0L) return@transaction false

        val ourNewBalance = balance.addWithoutOverflow(-amount)
        if (ourNewBalance == null || ourNewBalance < 0L) return@transaction false
        val theirNewBalance = other.balance.addWithoutOverflow(amount) ?: return@transaction false
        // all good to run transaction
        balance = ourNewBalance
        other.balance = theirNewBalance
        true
    }

    /** The username of this player. */
    val username
        get() = Db.transaction { Users.select { Users.uniqueId eq uuid }.first()[Users.username] }

    var nickname
        get() = Db.transaction { Users.select { Users.uniqueId eq uuid }.first()[Users.nickname] }
        set(value) {
            if (value == null || value.length <= Users.MAX_NICKNAME_LENGTH) {
                Db.transaction {
                    Users.update({ Users.uniqueId eq uuid }) {
                        it[nickname] = value
                    }
                }
                val player = Bukkit.getPlayer(uuid)
                if (player != null) {
                    CoroutineThreadPool.launch {
                        NameFormatter.refreshPlayerName(player)
                    }
                }
            }
        }

    var socialSpy
        get() = Db.transaction { Users.select { Users.uniqueId eq uuid }.first()[Users.socialSpy] }
        set(value) = Db.transaction {
            Users.update({ Users.uniqueId eq uuid }) {
                it[socialSpy] = value
            }
        }

    fun formattedName(): CompletableFuture<Component> {
        // get the nickname, or the username
        val name = Db.transaction {
            val row = Users.select { Users.uniqueId eq uuid }.first()
            row[Users.nickname]?.let { "*$it" } ?: row[Users.username]
        }
        return NameFormatter.formatUsername(uuid, name)
    }

    companion object {
        private const val PAGE_SIZE = 5

        fun lookup(uuid: UUID) = Db.transaction {
            if (!Users.select { Users.uniqueId eq uuid }.empty()) {
                User(uuid)
            } else {
                null
            }
        }

        fun register(player: Player) = Db.transaction {
            val uuid = player.uniqueId
            val name = player.name
            if (Users.select { Users.uniqueId eq uuid }.empty()) {
                Users.insert {
                    it[uniqueId] = uuid
                    it[username] = name
                }
                true
            } else {
                // Ensure we update the existing username of this player, in case it changed.
                Users.update({ Users.uniqueId eq uuid }) {
                    it[username] = name
                }
                false
            }
        }

        fun lookup(offlinePlayer: OfflinePlayer) = lookup(offlinePlayer.uniqueId)

        fun lookupCommand(offlinePlayer: OfflinePlayer) = lookup(offlinePlayer)
            ?: commandError(Messages["error.playerNotFound"])

        /** Page index starts at 1 */
        fun top(page: Int) = Db.transaction {
            Users.selectAll()
                .orderBy(Users.balance, SortOrder.DESC)
                .limit(PAGE_SIZE, (page - 1).toLong() * PAGE_SIZE)
                .map { User(it[Users.uniqueId]).formattedName().get() to it[Users.balance] }
        }

        fun spies() = Db.transaction {
            Users.select { Users.socialSpy eq true }
                .asSequence()
                .map { Bukkit.getPlayer(it[Users.uniqueId]) }
                .filterNotNull()
                .toList()
        }
    }
}
