package com.dekaustubh.repositories

import com.dekaustubh.db.DatabaseFactory
import com.dekaustubh.models.Room
import com.dekaustubh.models.Rooms
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Interface to interact with [Room] data.
 */
interface RoomRepository {
    /**
     * Creates a new room with [rootName].
     * @return [Room] if successfully created, null otherwise.
     */
    suspend fun createRoom(roomName: String): Room?

    /**
     * Fetches room with specific [id].
     * @return [Room] if found, null otherwise.
     */
    suspend fun getRoomById(id: Long): Room?

    /**
     * Fetches user with specific [name].
     * @return List of [Room] if found, empty otherwise.
     */
    suspend fun searchRoomByName(name: String): List<Room>

    /**
     * Deletes room specified by [id].
     * Note: This is not a hard delete, only [Room#deleted_at] field is set, i.e. a soft delete.
     * @return [true] if user is deleted, false otherwise.
     */
    suspend fun removeRoomById(id: Long): Boolean

    /**
     * Updates room given with [id].
     * @return Updated [Room], null otherwise.
     */
    suspend fun updateRoom(id: Long, updatedName: String, leaderboardId: Long): Room?
}

class RoomRepositoryImpl() : RoomRepository {
    override suspend fun createRoom(roomName: String): Room? {
        var key = 0L
        transaction {
            key = (Rooms.insert {
                it[name] = roomName
                it[created_at] = System.currentTimeMillis()
                it[leaderboard_id] = 0
            } get Rooms.id)

            commit()
        }
        return getRoomById(key)
    }

    override suspend fun getRoomById(id: Long): Room? {
        var room: Room? = null
        transaction {
            room = Rooms
                .select { (Rooms.id eq id) and (Rooms.deleted_at eq 0) }
                .mapNotNull { toRoom(it) }
                .singleOrNull()
        }
        return room
    }

    override suspend fun searchRoomByName(name: String): List<Room> {
        val rooms = mutableListOf<Room>()
        transaction {
            rooms.addAll(
                Rooms
                    .select { Rooms.name like name }
                    .mapNotNull { toRoom(it) }
            )
        }
        return rooms
    }

    override suspend fun removeRoomById(id: Long): Boolean {
        val room = DatabaseFactory.dbQuery {
            Rooms.update({ Rooms.id eq id }) {
                it[deleted_at] = System.currentTimeMillis()
            }
        }

        return room != null
    }

    override suspend fun updateRoom(id: Long, updatedName: String, leaderboardId: Long): Room? {
        transaction {
            Rooms.update({ (Rooms.id eq id) and (Rooms.deleted_at eq 0) }) {
                it[name] = updatedName
                it[created_at] = System.currentTimeMillis()
                it[updated_at] = System.currentTimeMillis()
            }
            commit()
        }
        return getRoomById(id)
    }

    private fun toRoom(row: ResultRow): Room =
        Room(
            row[Rooms.id],
            row[Rooms.name],
            row[Rooms.leaderboard_id]
        )
}