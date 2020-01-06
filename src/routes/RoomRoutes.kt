package com.dekaustubh.routes

import com.dekaustubh.constants.Response.USER_ATTR
import com.dekaustubh.interceptors.userInterceptor
import com.dekaustubh.models.*
import com.dekaustubh.repositories.RoomRepository
import com.dekaustubh.repositories.UserRepository
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

/**
 * All users related routes.
 */
fun Routing.roomsRoutes(roomRepository: RoomRepository, userRepository: UserRepository) {
    route("/api/v1") {
        route("/room") {
            intercept(ApplicationCallPipeline.Call) {
                userInterceptor(userRepository)
            }

            post("/create") {
                val user = call.attributes[USER_ATTR]
                val request = call.receive<CreateRoomRequest>()

                val addedUser = roomRepository.createRoom(request.name, user.id)
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
                val id = call.parameters["id"]?.toLong() ?: 0L
                val room = roomRepository.getRoomById(id)
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

            get("/{id}/matches") {
                val id = call.parameters["id"]?.toLong() ?: 0L
                val matches = roomRepository.getRoomMatches(id)
                call.respond(
                    HttpStatusCode.OK,
                    MatchesResult(
                        success = Success(success = "Matches fetched"),
                        matches = matches
                    )
                )
            }

            put("/{id}/join") {
                val id = call.parameters["id"]?.toLong() ?: 0L
                val user = call.attributes[USER_ATTR]

                val room = roomRepository.joinRoom(id, user.id)
                room?.let {
                    call.respond(
                        HttpStatusCode.OK,
                        RoomResult(
                            success = Success(success = "Room joined successfully"),
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

        route("/rooms") {
            intercept(ApplicationCallPipeline.Call) {
                userInterceptor(userRepository)
            }

            get("/") {
                val user = call.attributes[USER_ATTR]
                val rooms = roomRepository.getAllRoomsForUser(user.id)

                call.respond(
                    HttpStatusCode.OK,
                    RoomsResult(
                        success = Success(success = "Rooms fetched"),
                        rooms = rooms
                    )
                )
            }
        }
    }
}