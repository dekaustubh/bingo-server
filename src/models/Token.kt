package com.dekaustubh.models

import com.dekaustubh.models.User.Users
import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.exposed.sql.Table

data class Token(
    val id: Long,
    val token: String,
    @JsonProperty("user_id")
    val userId: Long
)

/**
 * Tokens database table.
 */
object Tokens : Table() {
    val id = long("id").primaryKey().autoIncrement()
    val token = varchar("token", 255)
    val user_id = long("user_id")
    val created_at = Users.long("created_at")
    val deleted_at = Users.long("deleted_at").default(0)
    val updated_at = Users.long("updated_at").default(0)
}