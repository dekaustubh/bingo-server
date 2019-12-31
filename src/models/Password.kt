package com.dekaustubh.models

import com.dekaustubh.models.User.Users
import org.jetbrains.exposed.dao.LongIdTable

data class Password(
    val userId: Long,
    val password: String
)

/**
 * Relation between user & password.
 */
object Passwords : LongIdTable() {
    val user_id = long("user_id") references Users.id
    val password = varchar("password", 32)
    val created_at = Rooms.long("created_at")
    val deleted_at = Rooms.long("deleted_at").default(0)
    val updated_at = Rooms.long("updated_at").default(0)
}

