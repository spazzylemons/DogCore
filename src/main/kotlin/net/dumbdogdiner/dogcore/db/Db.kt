package net.dumbdogdiner.dogcore.db

import net.dumbdogdiner.dogcore.DogCorePlugin
import net.dumbdogdiner.dogcore.db.tables.Mutes
import net.dumbdogdiner.dogcore.db.tables.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction

object Db {
    private lateinit var db: Database

    fun init(plugin: DogCorePlugin) {
        val database = plugin.getConfigString("db.database")
        val port = plugin.getConfigString("db.port")
        val username = plugin.getConfigString("db.username")
        val password = plugin.getConfigString("db.password")

        db = Database.connect(
            url = "jdbc:postgresql://localhost:$port/$database",
            driver = "org.postgresql.Driver",
            user = username,
            password = password
        )

        transaction {
            SchemaUtils.create(Mutes, Users)
        }
    }

    fun <T> transaction(statement: Transaction.() -> T): T =
        org.jetbrains.exposed.sql.transactions.transaction(db, statement)
}
