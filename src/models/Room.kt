package com.dekaustubh.models

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.exposed.sql.Table

/**
 * Data model for Room object.
 */
data class Room(
    val id: Long,
    val name: String,
    @JsonProperty("leaderboard_id")
    val leaderboardId: Long
)

/**
 * Room database table.
 */
object Rooms : Table() {
    val id = long("id").primaryKey().autoIncrement()
    val name = varchar("name", 255)
    val leaderboard_id = long("leaderboard_id")
    val created_at = long("created_at")
    val deleted_at = long("deleted_at").default(0)
    val updated_at = long("updated_at").default(0)
}

/**
 * Result class for fetching or updating single [Room].
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonClassDescription("room")
data class RoomResult(
    val error: Error? = null,
    val success: Success? = null,
    val room: Room?
)

/**
 * Result class for fetching all the users.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonClassDescription("rooms")
data class RoomsResult(
    val error: Error? = null,
    val success: Success? = null,
    val rooms: List<Rooms>
)