package com.dekaustubh.models.room

import com.dekaustubh.models.Error
import com.dekaustubh.models.Success
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonInclude

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