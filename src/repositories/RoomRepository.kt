package com.dekaustubh.repositories

import com.dekaustubh.constants.Db.LIMIT
import com.dekaustubh.constants.Db.OFFSET
import com.dekaustubh.db.DatabaseFactory
import com.dekaustubh.extensions.toLeaderboard
import com.dekaustubh.extensions.toMatch
import com.dekaustubh.extensions.toRoom
import com.dekaustubh.models.*
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
    suspend fun createRoom(roomName: String, createdBy: String): Room?

    /**
     * Fetches room with specific [id].
     * @return [Room] if found, null otherwise.
     */
    suspend fun getRoomById(id: Long): Room?

    /**
     * Fetches matches of with specific [id].
     * @return [List<Match>] if found, empty otherwise.
     */
    suspend fun getRoomMatches(id: Long): List<Match>

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
    suspend fun getAllRoomsForUser(userId: String, offset: Int = OFFSET, limit: Int = LIMIT): List<Room>

    /**
     * User with [userId] joins a room with [roomId].
     * @return Updated [Room], null otherwise.
     */
    suspend fun joinRoom(roomId: Long, userId: String): Room?

    /**
     * Gets all the joinees of the room
     */
    fun getRoomMembers(roomId: Long): List<String>
}

class RoomRepositoryImpl() : RoomRepository {
    override suspend fun createRoom(roomName: String, createdBy: String): Room? {
        var key = 0L
        transaction {
            key = (Rooms.insert {
                it[name] = roomName
                it[created_at] = TimeUtil.getCurrentUtcMillis()
                it[created_by] = createdBy
                it[leaderboard_id] = ""
            } get Rooms.id)

            // Make an entry to the leaderboards...
            Leaderboards.insert {
                it[room_id] = key
                it[user_id] = createdBy
                it[created_at] = TimeUtil.getCurrentUtcMillis()
            }

            RoomMembers.insert {
                it[room_id] = key
                it[user_id] = createdBy
                it[created_at] = TimeUtil.getCurrentUtcMillis()
            }

            commit()
        }
        return getRoomById(key)
    }

    override suspend fun getRoomById(id: Long): Room? {
        var room: Room? = null
        val leaderboars = mutableListOf<Leaderboard>()
        transaction {
            Leaderboards.select { Leaderboards.room_id eq id }
                .orderBy(Leaderboards.points)
                .forEach { leaderboars.add(it.toLeaderboard()) }

            room = Rooms
                .select { (Rooms.id eq id) and (Rooms.deleted_at eq 0) }
                .mapNotNull { it.toRoom(getRoomMembers(id), leaderboars) }
                .singleOrNull()
        }
        return room
    }

    override suspend fun getRoomMatches(id: Long): List<Match> {
        val matches = mutableListOf<Match>()
        transaction {
            Matches.select { Matches.room_id eq id }
                .orderBy(Matches.points)
                .forEach { matches.add(it.toMatch()) }
        }
        return matches
    }

    override suspend fun searchRoomByName(name: String): List<Room> {
        val rooms = mutableListOf<Room>()
        transaction {
            rooms.addAll(
                Rooms
                    .select { Rooms.name like name }
                    .mapNotNull { it.toRoom() }
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

    override suspend fun getAllRoomsForUser(userId: String, offset: Int, limit: Int): List<Room> {
        val rooms = mutableListOf<Room>()
        transaction {
            Rooms.innerJoin(RoomMembers)
                .slice(Rooms.columns)
                .select { Rooms.id.eq(RoomMembers.room_id) and (RoomMembers.user_id.eq(userId)) }
                .limit(limit, offset)
                .orderBy(Rooms.created_at)
                .forEach { rooms.add(it.toRoom()) }
        }
        return rooms
    }

    override suspend fun joinRoom(roomId: Long, userId: String): Room? {
        transaction {
            RoomMembers.insert {
                it[room_id] = roomId
                it[user_id] = userId
                it[created_at] = TimeUtil.getCurrentUtcMillis()
            }

            // Make an entry to the leaderboards...
            Leaderboards.insert {
                it[room_id] = roomId
                it[user_id] = userId
                it[created_at] = TimeUtil.getCurrentUtcMillis()
            }
            commit()
        }
        return getRoomById(roomId)
    }

    override fun getRoomMembers(roomId: Long): List<String> {
        val members = mutableListOf<String>()
        transaction {
            RoomMembers.select { RoomMembers.room_id.eq(roomId) }
                .forEach { members.add(it[RoomMembers.user_id]) }
        }
        return members
    }
}