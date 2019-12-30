package com.dekaustubh.db

import com.dekaustubh.models.Users
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
        }
    }

    suspend fun <T> dbQuery(
        block: suspend () -> T
    ): T = newSuspendedTransaction { block() }
}