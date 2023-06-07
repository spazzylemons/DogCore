package net.dumbdogdiner.dogcore.db

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.update

fun <T : Table> T.upsert(where: SqlExpressionBuilder.() -> Op<Boolean>, body: T.(UpdateBuilder<Int>) -> Unit) {
    if (update(where) { body(it) } == 0) {
        insert(body)
    }
}
