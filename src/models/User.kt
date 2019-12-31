package com.dekaustubh.models.User

import com.dekaustubh.models.Error
import com.dekaustubh.models.Room
import com.dekaustubh.models.Success
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonInclude
import org.jetbrains.exposed.sql.Table

/**
 * Data model for User.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val token: String? = null,
    val rooms: List<Room>? = null
)

data class LoginRequest(
    val name: String,
    val email: String,
    val password: String
)

/**
 * User database table.
 */
object Users : Table() {
    val id = long("id").primaryKey().autoIncrement()
    val name = varchar("name", 255)
    val email = varchar("email", 50)
    val created_at = long("created_at")
    val deleted_at = long("deleted_at").default(0)
    val updated_at = long("updated_at").default(0)
    init {
        index(true, email)
    }
}

/**
 * Result class for fetching or updating single user.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonClassDescription("user")
data class UserResult(
    val error: Error? = null,
    val success: Success? = null,
    val user: User?
)

/**
 * Result class for fetching all the users.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonClassDescription("users")
data class UsersResult(
    val error: Error? = null,
    val success: Success? = null,
    val user: List<User>
)