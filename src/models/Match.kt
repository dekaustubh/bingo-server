package com.dekaustubh.models

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.exposed.sql.Table

/**
 * Data model for Match object.
 */
data class Match(
    val id: Long,
    val name: String,
    @JsonProperty("room_id")
    val roomId: Long,
    @JsonProperty("created_by")
    val createdBy: String,
    val players: MutableList<String>,
    @JsonProperty("winner_id")
    val winnerId: String,
    @JsonProperty("status")
    val status: MatchStatus
)

data class TakeTurn(
    val number: Int,
    @JsonProperty("current_turn")
    val currentTaker: String,
    @JsonProperty("next_turn")
    val nextTaker: String
)

/**
 * Match database table.
 */
object Matches : Table() {
    val id = long("id").primaryKey().autoIncrement()
    val name = varchar("name", 24)
    val created_by = text("created_by")
    val room_id = long("room_id")
    val players = text("players").nullable()
    val winner_id = text("winner_id").nullable()
    val points = integer("points").default(0)
    val status = varchar("status", 20).default(MatchStatus.WAITING.toString())
    val created_at = long("created_at")
    val deleted_at = long("deleted_at").default(0)
    val updated_at = long("updated_at").default(0)
}

enum class MatchStatus {
    WAITING,
    STARTED,
    ENDED
}

/**
 * Result class for fetching or updating single [Match].
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonClassDescription("match")
data class MatchResult(
    val error: Error? = null,
    val success: Success? = null,
    val match: Match?
)

/**
 * Result class for fetching all the users.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonClassDescription("matches")
data class MatchesResult(
    val error: Error? = null,
    val success: Success? = null,
    val matches: List<Match>
)