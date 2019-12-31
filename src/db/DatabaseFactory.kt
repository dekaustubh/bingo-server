package com.dekaustubh.db

import com.dekaustubh.models.*
import com.dekaustubh.models.User.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        Database.connect(
            driver = "org.postgresql.Driver",
            url = "jdbc:postgresql://localhost:5432/postgres",
            user = "postgres",
            password = "kaustubh"
        )
        transaction {
            create(Users)
            create(Tokens)
            create(Rooms)
            create(Matches)
            create(RoomMembers)
        }
    }

    suspend fun <T> dbQuery(
        block: suspend () -> T
    ): T = newSuspendedTransaction { block() }
}