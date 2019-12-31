package com.dekaustubh.repositories

import com.dekaustubh.constants.Db
import com.dekaustubh.constants.Db.LIMIT
import com.dekaustubh.constants.Db.OFFSET
import com.dekaustubh.db.DatabaseFactory.dbQuery
import com.dekaustubh.extensions.toPassword
import com.dekaustubh.extensions.toRoom
import com.dekaustubh.extensions.toToken
import com.dekaustubh.extensions.toUser
import com.dekaustubh.models.*
import com.dekaustubh.models.User.User
import com.dekaustubh.models.User.Users
import com.dekaustubh.utils.TimeUtil
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

/**
 * Interface to interact with user data.
 */
interface UserRepository {
    /**
     * Creates a row in Users database table.
     * @return [User] if successfully created, null otherwise.
     */
    suspend fun register(userName: String, userEmail: String, userPassword: String): User?

    /**
     * Fetches user with specific [id].
     * @return [User] if found, null otherwise.
     */
    suspend fun getUserById(id: Long): User?

    /**
     * Fetches user with specific [email].
     * @return [User] if found, null otherwise.
     */
    suspend fun getUserByEmail(email: String): User?

    /**
     * Deletes user specified by [id].
     * Note: This is not a hard delete, only [User#deleted_at] field is set, i.e. a soft delete.
     * @return [true] if user is deleted, false otherwise.
     */
    suspend fun removeUserById(id: Long): Boolean

    /**
     * Fetches user with specific [userToken].
     * @return [User] if found, null otherwise.
     */
    suspend fun getUserByToken(userToken: String): User?

    /**
     * Logs user in.
     * @return [User] if successfully found, null otherwise.
     */
    suspend fun login(userEmail: String, userPassword: String): User?

    /**
     * Chekcs if user is present with the [userEmail].
     * @return [true] if user is present, false otherwise.
     */
    suspend fun isUserPresentWithEmail(userEmail: String): Boolean
}

class UserRepositoryImpl() : UserRepository {
    override suspend fun register(userName: String, userEmail: String, userPassword: String): User? {
        try {
            var key = 0L
            transaction {
                key = Users.insert {
                    it[name] = userName
                    it[email] = userEmail
                    it[created_at] = TimeUtil.getCurrentUtcMillis()
                } get Users.id
                commit()
            }

            val user = getUserById(key)
            var uuid = ""
            user?.let { u ->
                transaction {
                    Tokens.insert {
                        uuid = UUID.randomUUID().toString()
                        it[token] = uuid
                        it[user_id] = u.id
                        it[created_at] = TimeUtil.getCurrentUtcMillis()
                    }

                    Passwords.insert {
                        it[user_id] = key
                        it[password] = userPassword
                        it[created_at] = TimeUtil.getCurrentUtcMillis()
                    }

                    commit()
                }
            }
            return if (user != null) {
                User(
                    user.id,
                    user.name,
                    user.email,
                    uuid
                )
            } else {
                null
            }
        } catch (e: ExposedSQLException) {
            println("Error while adding user $e")
            return null
        }
    }

    override suspend fun getUserById(id: Long): User? {
        var user: User? = null
        transaction {
            user = Users
                .select { (Users.id eq id) and (Users.deleted_at eq 0) }
                .mapNotNull { it.toUser() }
                .singleOrNull()
        }
        return user
    }

    override suspend fun getUserByEmail(email: String): User? {
        var user: User? = null
        var token: Token? = null
        val rooms = mutableListOf<Room>()
        transaction {
            token = Tokens
                .select { (Tokens.token eq (user?.token ?: "")) and (Tokens.deleted_at eq 0) }
                .mapNotNull { it.toToken() }
                .singleOrNull()

            user = Users
                .select { (Users.email eq email) and (Users.deleted_at eq 0) }
                .mapNotNull { it.toUser(token?.token) }
                .singleOrNull()

            val userId = user?.id ?: 0L
            Rooms.innerJoin(RoomMembers)
                .slice(Rooms.columns)
                .select { Rooms.id.eq(RoomMembers.room_id) and (RoomMembers.user_id.eq(userId)) }
                .limit(LIMIT, OFFSET)
                .orderBy(Rooms.created_at)
                .forEach { rooms.add(it.toRoom()) }
        }
        return user
    }

    override suspend fun removeUserById(id: Long): Boolean {
        val user = dbQuery {
            Users.update({ Users.id eq id }) {
                it[deleted_at] = TimeUtil.getCurrentUtcMillis()
            }
        }

        return user != null
    }

    override suspend fun getUserByToken(userToken: String): User? {
        var user: User? = null
        var token: Token? = null
        transaction {
            token = Tokens
                .select { (Tokens.token eq userToken) and (Tokens.deleted_at eq 0) }
                .mapNotNull { it.toToken() }
                .singleOrNull()

            user = (Users innerJoin Tokens)
                .slice(Users.columns)
                .select { (Users.id.eq(Tokens.user_id) and Tokens.token.eq(token?.token ?: "---")) }
                .mapNotNull { it.toUser(token?.token) }
                .singleOrNull()

        }
        return user
    }

    override suspend fun login(userEmail: String, userPassword: String): User? {
        val user: User? = getUserByEmail(email = userEmail) ?: return null
        var password: Password? = null
        transaction {
            password =
                Passwords
                    .select { Passwords.user_id eq (user?.id ?: 0L) }
                    .mapNotNull { it.toPassword() }
                    .singleOrNull()
        }

        if (password == null) return null
        return user
    }

    override suspend fun isUserPresentWithEmail(userEmail: String): Boolean {
        return getUserByEmail(userEmail) != null
    }
}