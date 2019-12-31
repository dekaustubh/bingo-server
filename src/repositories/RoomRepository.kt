package com.dekaustubh.repositories

import com.dekaustubh.constants.Db.LIMIT
import com.dekaustubh.constants.Db.OFFSET
import com.dekaustubh.db.DatabaseFactory
import com.dekaustubh.models.Room
import com.dekaustubh.models.RoomMembers
import com.dekaustubh.models.Rooms
import com.dekaustubh.utils.TimeUtil
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
    suspend fun createRoom(roomName: String, createdBy: Long): Room?

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

    /**
     * Fetches all the rooms for user who is either creator or member of the room.
     * @return List<Room> if user is part of room.
     */
    suspend fun getAllRoomsForUser(userId: Long, offset: Int = OFFSET, limit: Int = LIMIT): List<Room>

    /**
     * User with [userId] joins a room with [roomId].
     * @return Updated [Room], null otherwise.
     */
    suspend fun joinRoom(roomId: Long, userId: Long): Room?
}

class RoomRepositoryImpl() : RoomRepository {
    override suspend fun createRoom(roomName: String, createdBy: Long): Room? {
        var key = 0L
        transaction {
            key = (Rooms.insert {
                it[name] = roomName
                it[created_at] = TimeUtil.getCurrentUtcMillis()
                it[created_by] = createdBy
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
                .mapNotNull { toRoom(it, getRoomMembers(id)) }
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
                it[deleted_at] = TimeUtil.getCurrentUtcMillis()
            }
        }

        return room != null
    }

    override suspend fun updateRoom(id: Long, updatedName: String, leaderboardId: Long): Room? {
        transaction {
            Rooms.update({ (Rooms.id eq id) and (Rooms.deleted_at eq 0) }) {
                it[name] = updatedName
                it[created_at] = TimeUtil.getCurrentUtcMillis()
                it[updated_at] = TimeUtil.getCurrentUtcMillis()
            }
            commit()
        }
        return getRoomById(id)
    }

    override suspend fun getAllRoomsForUser(userId: Long, offset: Int, limit: Int): List<Room> {
        val rooms = mutableListOf<Room>()
        transaction {
            Rooms.innerJoin(RoomMembers)
                .slice(Rooms.columns)
                .select { Rooms.id.eq(RoomMembers.room_id) and (RoomMembers.user_id.eq(userId)) }
                .limit(limit, offset)
                .orderBy(Rooms.created_at)
                .forEach { rooms.add(toRoom(it)) }
        }
        return rooms
    }

    override suspend fun joinRoom(roomId: Long, userId: Long): Room? {
        transaction {
            RoomMembers.insert {
                it[room_id] = roomId
                it[user_id] = userId
                it[created_at] = TimeUtil.getCurrentUtcMillis()
            }

            commit()
        }
        return getRoomById(roomId)
    }

    private fun getRoomMembers(roomId: Long, limit: Int = LIMIT, offset: Int = OFFSET): List<Long> {
        val members = mutableListOf<Long>()
        RoomMembers.select { RoomMembers.room_id.eq(roomId) }
            .limit(limit, offset)
            .forEach { members.add(it[RoomMembers.user_id]) }
        return members
    }

    private fun toRoom(row: ResultRow, members: List<Long> = emptyList()): Room =
        Room(
            row[Rooms.id],
            row[Rooms.name],
            row[Rooms.leaderboard_id],
            row[Rooms.created_by],
            members
        )
}