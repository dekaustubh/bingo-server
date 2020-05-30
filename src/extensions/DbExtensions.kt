package com.dekaustubh.extensions

import com.dekaustubh.models.*
import com.dekaustubh.models.User.User
import com.dekaustubh.models.User.Users
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toRoom(members: List<String> = emptyList(), leaderboards: List<Leaderboard> = emptyList()): Room =
    Room(
        this[Rooms.id],
        this[Rooms.name],
        this[Rooms.leaderboard_id],
        this[Rooms.created_by],
        members,
        leaderboards
    )

fun ResultRow.toUser(rooms: List<Room> = emptyList()): User =
    User(
        this[Users.id],
        this[Users.name],
        this[Users.token],
        rooms
    )

fun ResultRow.toMatch(): Match =
    Match(
        this[Matches.id],
        this[Matches.name],
        this[Matches.room_id],
        this[Matches.created_by],
        this[Matches.players]?.toPlayers() ?: mutableListOf(),
        this[Matches.winner_id] ?: "",
        MatchStatus.valueOf(this[Matches.status])
    )

fun ResultRow.toLeaderboard(): Leaderboard =
    Leaderboard(
        this[Leaderboards.user_id],
        this[Leaderboards.room_id],
        this[Leaderboards.points]
    )