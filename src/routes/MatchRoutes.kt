package com.dekaustubh.routes

import com.dekaustubh.models.Error
import com.dekaustubh.models.Match
import com.dekaustubh.models.MatchResult
import com.dekaustubh.models.Success
import com.dekaustubh.models.User.User
import com.dekaustubh.models.User.UserResult
import com.dekaustubh.repositories.MatchRepository
import com.dekaustubh.repositories.UserRepository
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

/**
 * All match related routes.
 */
fun Routing.matchRoutes(matchRepository: MatchRepository) {
    route("/api/v1") {
        route("/match") {
            post("/create") {
                val match = call.receive<Match>()
                // TODO.. change match.createdBy to basic auth.
                val addedMatch = matchRepository.createMatch(match.createdBy, match.roomId)
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

            get("/{id}") {
                val id = call.parameters["id"]
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
                // TODO.. change match.createdBy to basic auth.
                val match = matchRepository.joinMatch(matchId?.toLong() ?: 0L, 1L)
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

            put("/{id}/update") {
                val matchId = call.parameters["id"]
                val oldMatch = call.receive<Match>()
                // TODO.. change match.createdBy to basic auth.
                val match = matchRepository.updateMatch(matchId?.toLong() ?: 0L, 1L, oldMatch.players, 100)
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