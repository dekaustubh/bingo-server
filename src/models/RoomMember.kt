package com.dekaustubh.models

import com.dekaustubh.models.User.Users
import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.exposed.dao.LongIdTable

data class RoomMember(
    @JsonProperty("room_id")
    val roomId: Long,
    @JsonProperty("user_id")
    val userId: Long
)

object RoomMembers: LongIdTable() {
    val room_id = long("room_id") references (Rooms.id)
    val user_id = long("user_id") references (Users.id)
    val created_at = Rooms.long("created_at")
    val deleted_at = Rooms.long("deleted_at").default(0)
    val updated_at = Rooms.long("updated_at").default(0)
}