package com.dekaustubh.db

import com.dekaustubh.models.*
import com.dekaustubh.models.User.Users
import io.ktor.application.ApplicationEnvironment
import io.ktor.util.KtorExperimentalAPI
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalStateException

object DatabaseFactory {
    @KtorExperimentalAPI
    fun init(env: ApplicationEnvironment) {
        val dburl: String = env.config.propertyOrNull("ktor.deployment.dburl")?.getString() ?: throw IllegalStateException("dburl not present")
        val dbuser: String = env.config.propertyOrNull("ktor.deployment.dbuser")?.getString() ?: throw IllegalStateException("dbuser not present")
        val dbpassword: String = env.config.propertyOrNull("ktor.deployment.dbpassword")?.getString() ?: throw IllegalStateException("dbpassword not present")

        Database.connect(
            driver = "org.postgresql.Driver",
            url = dburl,
            user = dbuser,
            password = dbpassword
        )
        transaction {
            create(Users)
            create(Tokens)
            create(Rooms)
            create(Matches)
            create(RoomMembers)
            create(Leaderboards)
            create(UserDevices)
        }
    }

    suspend fun <T> dbQuery(
        block: suspend () -> T
    ): T = newSuspendedTransaction { block() }
}