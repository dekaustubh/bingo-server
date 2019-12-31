package com.dekaustubh.models

import com.dekaustubh.models.User.Users
import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.exposed.dao.LongIdTable

data class Leaderboard(
    @JsonProperty("user_id")
    val userId: Long,
    @JsonProperty("room_id")
    val roomId: Long,
    val points: Int
)

object Leaderboards : LongIdTable() {
    val user_id = long("user_id") references Users.id
    val room_id = long("room_id") references Rooms.id
    val points = integer("points").default(0)
    val created_at = long("created_at")
    val deleted_at = long("deleted_at").default(0)
    val updated_at = long("updated_at").default(0)
}