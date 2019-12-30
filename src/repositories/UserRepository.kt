package com.dekaustubh.repositories

import com.dekaustubh.db.DatabaseFactory.dbQuery
import com.dekaustubh.models.User
import com.dekaustubh.models.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

interface UserRepository {
    suspend fun addUser(userName: String, userEmail: String): User?
    suspend fun getUserById(id: Long): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun removeUserById(id: Long): Boolean
}

class UserRepositoryImpl() : UserRepository {
    override suspend fun addUser(userName: String, userEmail: String): User? {
        transaction {
            Users.insert {
                it[name] = userName
                it[email] = userEmail
                it[created_at] = System.currentTimeMillis()
            }
            commit()
        }
        return getUserByEmail(userEmail)
    }

    override suspend fun getUserById(id: Long): User? {
        var user: User? = null
        transaction {
            user = Users
                .select { (Users.id eq id) and (Users.deleted_at eq 0) }
                .mapNotNull { toUser(it) }
                .singleOrNull()
        }
        return user
    }

    override suspend fun getUserByEmail(email: String): User? {
        var user: User? = null
        transaction {
            user = Users
                .select { (Users.email eq email) and (Users.deleted_at eq 0) }
                .mapNotNull { toUser(it) }
                .singleOrNull()
        }
        return user
    }

    override suspend fun removeUserById(id: Long): Boolean {
        val user = dbQuery {
            Users.update({ Users.id eq id }) {
                it[deleted_at] = System.currentTimeMillis()
            }
        }

        return user != null
    }

    private fun toUser(row: ResultRow): User = User(row[Users.id], row[Users.name], row[Users.email])
}