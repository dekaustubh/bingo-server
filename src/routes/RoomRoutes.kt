package com.dekaustubh.routes

import com.dekaustubh.models.Error
import com.dekaustubh.models.Room
import com.dekaustubh.models.RoomResult
import com.dekaustubh.models.Success
import com.dekaustubh.repositories.RoomRepository
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

/**
 * All users related routes.
 */
fun Routing.roomsRoutes(roomRepository: RoomRepository) {
    route("/api/v1") {
        route("/room") {
            post("/create") {
                val room = call.receive<Room>()
                val addedUser = roomRepository.createRoom(room.name)
                addedUser?.let {
                    call.respond(
                        HttpStatusCode.Created,
                        RoomResult(
                            success = Success(success = "Room created"),
                            room = it
                        )
                    )
                } ?: run {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        RoomResult(
                            error = Error(error = "Could not create room"),
                            room = null
                        )
                    )
                }
            }

            get("/{id}") {
                val id = call.parameters["id"]
                val room = roomRepository.getRoomById(id?.toLong() ?: 0L)
                room?.let {
                    call.respond(
                        HttpStatusCode.OK,
                        RoomResult(
                            success = Success(success = "Room fetched"),
                            room = it
                        )
                    )
                } ?: run {
                    call.respond(
                        HttpStatusCode.NotFound,
                        RoomResult(
                            error = Error(error = "Room not present"),
                            room = null
                        )
                    )
                }
            }
        }
    }
}