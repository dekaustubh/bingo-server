package com.dekaustubh.routes

import com.dekaustubh.constants.Response
import com.dekaustubh.interceptors.userInterceptor
import com.dekaustubh.models.Error
import com.dekaustubh.models.Match
import com.dekaustubh.models.MatchResult
import com.dekaustubh.models.Success
import com.dekaustubh.models.User.User
import com.dekaustubh.models.User.UserResult
import com.dekaustubh.repositories.MatchRepository
import com.dekaustubh.repositories.UserRepository
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.util.AttributeKey

/**
 * All match related routes.
 */
fun Routing.matchRoutes(matchRepository: MatchRepository, userRepository: UserRepository) {
    route("/api/v1/room/{roomId}") {
        route("/match") {
            intercept(ApplicationCallPipeline.Call) {
                val roomId = call.parameters["roomId"]?.toLong() ?: 0L
                if (roomId == 0L) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        MatchResult(
                            error = Error(error = "Invalid room id"),
                            match = null
                        )
                    )
                    return@intercept finish()
                }
                userInterceptor(userRepository)
            }

            post("/create") {
                val user = call.attributes[AttributeKey(Response.USER)] as User
                val roomId = call.parameters["roomId"]!!.toLong()

                val addedMatch = matchRepository.createMatch(user.id, roomId)
                addedMatch?.let {
                    call.respond(
                        HttpStatusCode.Created,
                        MatchResult(
                            success = Success(success = "Match created"),
                            match = it
                        )
                    )
                } ?: run {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        MatchResult(
                            error = Error(error = "Could not create match"),
                            match = null
                        )
                    )
                }
            }

            get("/{matchId}") {
                val id = call.parameters["matchId"]
                val match = matchRepository.getMatchById(id?.toLong() ?: 0L)

                match?.let {
                    call.respond(
                        HttpStatusCode.OK,
                        MatchResult(
                            success = Success(success = "Match fetched"),
                            match = it
                        )
                    )
                } ?: run {
                    call.respond(
                        HttpStatusCode.NotFound,
                        MatchResult(
                            error = Error(error = "Match not found"),
                            match = null
                        )
                    )
                }
            }

            put("/{id}/join") {
                val matchId = call.parameters["id"]
                val user = call.attributes[AttributeKey(Response.USER)] as User

                val match = matchRepository.joinMatch(matchId?.toLong() ?: 0L, user.id)
                match?.let {
                    call.respond(
                        HttpStatusCode.OK,
                        MatchResult(
                            success = Success(success = "Match updated successfully"),
                            match = it
                        )
                    )
                } ?: run {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        MatchResult(
                            error = Error(error = "Error updating match"),
                            match = null
                        )
                    )
                }
            }

            put("/{matchId}/update") {
                val matchId = call.parameters["matchId"]?.toLong() ?: 0L
                val oldMatch = call.receive<Match>()

                // TODO.. change points here.
                val match = matchRepository.updateMatch(matchId, oldMatch.winnerId, oldMatch.players, 100)
                match?.let {
                    call.respond(
                        HttpStatusCode.OK,
                        MatchResult(
                            success = Success(success = "Match updated successfully"),
                            match = it
                        )
                    )
                } ?: run {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        MatchResult(
                            error = Error(error = "Error updating match"),
                            match = null
                        )
                    )
                }
            }
        }
    }
}