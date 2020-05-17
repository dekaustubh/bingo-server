package com.dekaustubh.repositories

import com.dekaustubh.constants.Db.LIMIT
import com.dekaustubh.constants.Db.OFFSET
import com.dekaustubh.db.DatabaseFactory.dbQuery
import com.dekaustubh.extensions.toRoom
import com.dekaustubh.extensions.toUser
import com.dekaustubh.models.Room
import com.dekaustubh.models.RoomMembers
import com.dekaustubh.models.Rooms
import com.dekaustubh.models.User.User
import com.dekaustubh.models.User.Users
import com.dekaustubh.utils.TimeUtil
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

/**
 * Interface to interact with user data.
 */
interface UserRepository {
    /**
     * Creates a row in Users database table.
     * @return [User] if successfully created, null otherwise.
     */
    suspend fun register(userName: String, userId: String): User?

    /**
     * Fetches user with specific [id].
     * @return [User] if found, null otherwise.
     */
    suspend fun getUserById(id: String): User?

    /**
     * Deletes user specified by [id].
     * Note: This is not a hard delete, only [User#deleted_at] field is set, i.e. a soft delete.
     * @return [true] if user is deleted, false otherwise.
     */
    suspend fun removeUserById(id: String): Boolean

    /**
     * Logs user in.
     * @return [User] if successfully found, null otherwise.
     */
    suspend fun login(userId: String): User?
}

class UserRepositoryImpl() : UserRepository {
    override suspend fun register(userName: String, userId: String): User? {
        try {
            transaction {
                Users.insert {
                    it[name] = userName
                    it[id] = userId
                    it[token] = UUID.randomUUID().toString()
                    it[created_at] = TimeUtil.getCurrentUtcMillis()
                }
                commit()
            }

            return getUserById(userId)
        } catch (e: ExposedSQLException) {
            println("Error while adding user $e")
            return null
        }
    }

    override suspend fun getUserById(id: String): User? {
        var user: User? = null
        val rooms = mutableListOf<Room>()

        transaction {
            user = Users
                .select { (Users.id eq id) and (Users.deleted_at eq 0) }
                .mapNotNull { it.toUser() }
                .singleOrNull()

            val userId = user?.id ?: ""
            Rooms.innerJoin(RoomMembers)
                .slice(Rooms.columns)
                .select { Rooms.id.eq(RoomMembers.room_id) and (RoomMembers.user_id.eq(userId)) }
                .limit(LIMIT, OFFSET)
                .orderBy(Rooms.created_at)
                .forEach { rooms.add(it.toRoom()) }
        }
        return user?.let { User(it.id, it.name, it.token, it.rooms) }
    }

    override suspend fun removeUserById(id: String): Boolean {
        val user = dbQuery {
            Users.update({ Users.id eq id }) {
                it[deleted_at] = TimeUtil.getCurrentUtcMillis()
            }
        }

        return user != null
    }

    override suspend fun login(userId: String): User? {
        return getUserById(userId)
    }
}