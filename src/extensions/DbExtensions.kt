package com.dekaustubh.extensions

import com.dekaustubh.models.*
import com.dekaustubh.models.User.User
import com.dekaustubh.models.User.Users
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toRoom(members: List<Long> = emptyList(), leaderboards: List<Leaderboard> = emptyList()): Room =
    Room(
        this[Rooms.id],
        this[Rooms.name],
        this[Rooms.leaderboard_id],
        this[Rooms.created_by],
        members,
        leaderboards
    )

fun ResultRow.toUser(token: String? = null, rooms: List<Room> = emptyList()): User =
    User(
        this[Users.id],
        this[Users.name],
        this[Users.email],
        token,
        rooms
    )

fun ResultRow.toToken(): Token =
    Token(
        this[Tokens.id],
        this[Tokens.token],
        this[Tokens.user_id]
    )

fun ResultRow.toPassword(): Password =
    Password(
        this[Passwords.user_id],
        this[Passwords.password]
    )

fun ResultRow.toMatch(): Match =
    Match(
        this[Matches.id],
        this[Matches.room_id],
        this[Matches.created_by],
        this[Matches.players]?.toPlayers() ?: mutableListOf(),
        this[Matches.winner_id] ?: 0L,
        this[Matches.status]
    )

fun ResultRow.toLeaderboard(): Leaderboard =
    Leaderboard(
        this[Leaderboards.user_id],
        this[Leaderboards.room_id],
        this[Leaderboards.points]
    )