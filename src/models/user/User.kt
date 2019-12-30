package com.dekaustubh.models.User

import org.jetbrains.exposed.sql.Table

/**
 * Data model for User.
 */
data class User(
    val id: Long,
    val name: String,
    val email: String
)

/**
 * User database table.
 */
object Users : Table() {
    val id = long("id").primaryKey().autoIncrement()
    val name = varchar("name", 255)
    val email = varchar("email", 255)
    val created_at = long("created_at")
    val deleted_at = long("deleted_at").default(0)
    val updated_at = long("updated_at").default(0)
}