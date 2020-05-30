package com.dekaustubh.routes

import com.dekaustubh.constants.Response.USER_ATTR
import com.dekaustubh.interceptors.userInterceptor
import com.dekaustubh.models.*
import com.dekaustubh.repositories.LeaderboardRepository
import com.dekaustubh.repositories.MatchRepository
import com.dekaustubh.repositories.RoomRepository
import com.dekaustubh.repositories.UserRepository
import com.dekaustubh.socket.WebSocket
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

/**
 * All match related routes.
 */
fun Routing.matchRoutes(
    matchRepository: MatchRepository,
    userRepository: UserRepository,
    roomRepository: RoomRepository,
    leaderboardRepository: LeaderboardRepository,
    webSocket: WebSocket,
    mapper: ObjectMapper
) {
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
                val user = call.attributes[USER_ATTR]
                val name = call.request.queryParameters["name"] ?: throw IllegalStateException("Match name must be present")
                val roomId = call.parameters["roomId"]!!.toLong()

                val addedMatch = matchRepository.createMatch(user.id, roomId, name)

                val participants = roomRepository.getRoomMembers(roomId)
                participants.forEach {
                    webSocket.sendTo(
                        it,
                        mapper.writeValueAsString(
                            MatchCreated(
                                userId = user.id,
                                userName = user.name,
                                matchId = addedMatch?.id ?: throw IllegalArgumentException("Match should not be null"),
                                roomId = roomId
                            )
                        )
                    )
                }

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

            put("/{matchId}/join") {
                val matchId = call.parameters["matchId"]?.toLong() ?: 0L
                val user = call.attributes[USER_ATTR]
                val roomId = call.parameters["roomId"]?.toLong() ?: 0L
                val oldMatch = matchRepository.getMatchById(matchId)

                if (oldMatch?.status != MatchStatus.WAITING) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        MatchResult(
                            error = Error(error = "Match over already!"),
                            match = null
                        )
                    )
                    return@put
                }

                val userIds = oldMatch.players

                if (oldMatch.players.contains(user.id)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        MatchResult(
                            error = Error(error = "Match joined already"),
                            match = null
                        )
                    )
                    return@put
                }

                userIds.add(user.id)
                val newMatch = matchRepository.updateMatch(
                    matchId,
                    "",
                    oldMatch.status,
                    userIds
                )

                newMatch?.let { m ->
                    m.players
                        .filter { it != user.id }
                        .forEach { id ->
                            webSocket.sendTo(
                                id,
                                mapper.writeValueAsString(
                                    MatchJoined(userId = user.id, userName = user.name, matchId = m.id, roomId = roomId)
                                )
                            )
                        }

                    call.respond(
                        HttpStatusCode.OK,
                        MatchResult(
                            success = Success(success = "Match joined successfully"),
                            match = m
                        )
                    )
                } ?: run {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        MatchResult(
                            error = Error(error = "Error joining match"),
                            match = null
                        )
                    )
                }
            }

            post("/{matchId}/start") {
                val matchId = call.parameters["matchId"]?.toLong() ?: 0L
                val user = call.attributes[USER_ATTR]
                val roomId = call.parameters["roomId"]!!.toLong()

                val match = matchRepository.updateMatchStatus(matchId, MatchStatus.STARTED)

                match?.let { m ->
                    m.players
                        .filter { it != user.id }
                        .forEach { id ->
                            webSocket.sendTo(
                                id,
                                mapper.writeValueAsString(
                                    MatchStarted(userId = user.id, userName = user.name, matchId = m.id, roomId = roomId)
                                )
                            )
                        }

                    call.respond(
                        HttpStatusCode.OK,
                        MatchResult(
                            success = Success(success = "Match started successfully"),
                            match = m
                        )
                    )
                } ?: run {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        MatchResult(
                            error = Error(error = "Error starting match"),
                            match = null
                        )
                    )
                }
            }

            put("/{matchId}/takeTurn") {
                val matchId = call.parameters["matchId"]?.toLong() ?: 0L
                val roomId = call.parameters["roomId"]?.toLong() ?: 0L
                val user = call.attributes[USER_ATTR]
                val match = matchRepository.getMatchById(matchId)
                val turn = call.receive<TakeTurn>()

                if (match == null) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        MatchResult(
                            error = Error(error = "Error taking turn"),
                            match = null
                        )
                    )
                    return@put
                }
                val next = when (val index = match.players.indexOf(turn.currentTaker)) {
                    match.players.size -> match.players[0]
                    else -> match.players[index + 1]
                }
                val nextUser = userRepository.getBasicUser(next)

                match.players
                    .filter { it != user.id }
                    .forEach { id ->
                        webSocket.sendTo(
                            id,
                            mapper.writeValueAsString(
                                MatchTurn(
                                    userId = user.id,
                                    userName = user.name,
                                    matchId = matchId,
                                    roomId = roomId,
                                    nextTurn = nextUser,
                                    number = turn.number
                                )
                            )
                        )
                    }

                call.respond(
                    HttpStatusCode.OK,
                    MatchResult(
                        success = Success(success = "Match started successfully"),
                        match = match
                    )
                )
            }

            put("/{matchId}/update") {
                val matchId = call.parameters["matchId"]?.toLong() ?: 0L
                val oldMatch = call.receive<Match>()

                val match = matchRepository.updateMatch(
                    matchId,
                    oldMatch.winnerId,
                    oldMatch.status,
                    oldMatch.players,
                    0
                )
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

            put("/{matchId}/win") {
                val matchId = call.parameters["matchId"]?.toLong() ?: 0L
                val roomId = call.parameters["roomId"]?.toLong() ?: 0L
                val user = call.attributes[USER_ATTR]

                val oldMatch = matchRepository.getMatchById(matchId)
                val points = (oldMatch?.players?.size ?: 0) * 5
                val match = matchRepository.winMatch(
                    matchId,
                    user.id,
                    points
                )

                leaderboardRepository.updateLeaderboardForRoom(
                    roomId,
                    user.id,
                    points
                )

                match?.let { m ->
                    m.players
                        .filter { it != user.id }
                        .forEach { id ->
                            webSocket.sendTo(
                                id,
                                mapper.writeValueAsString(
                                    MatchWon(
                                        userId = user.id,
                                        points = points,
                                        matchId = matchId,
                                        roomId = roomId
                                    )
                                )
                            )
                        }

                    call.respond(
                        HttpStatusCode.OK,
                        MatchResult(
                            success = Success(success = "Match over!"),
                            match = m
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