package com.dekaustubh

import com.dekaustubh.db.DatabaseFactory
import com.dekaustubh.models.UserConnected
import com.dekaustubh.models.UserJoined
import com.dekaustubh.repositories.*
import com.dekaustubh.routes.matchRoutes
import com.dekaustubh.routes.roomsRoutes
import com.dekaustubh.routes.userRoutes
import com.dekaustubh.socket.WebSocket
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.content.*
import io.ktor.http.content.*
import io.ktor.features.*
import io.ktor.websocket.*
import io.ktor.http.cio.websocket.*
import java.time.*
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.jackson.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val webSocket = WebSocket()
    val mapper = jacksonObjectMapper()

    install(DefaultHeaders)
    install(CallLogging)

    install(io.ktor.websocket.WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    DatabaseFactory.init(environment)
    install(Routing) {
        val userRepository = UserRepositoryImpl()
        val leaderboardRepository = LeaderboardRepositoryImpl()
        userRoutes(userRepository)
        roomsRoutes(RoomRepositoryImpl(), userRepository)
        matchRoutes(MatchRepositoryImpl(), userRepository, leaderboardRepository, webSocket, mapper)
    }

    routing {
        // This defines a websocket `/connect` route that allows a protocol upgrade to convert a HTTP request/response request
        // into a bidirectional packetized connection.
        webSocket("/ws") {
            try {
                // We starts receiving messages (frames).
                // Since this is a coroutine. This coroutine is suspended until receiving frames.
                // Once the connection is closed, this consumeEach will finish and the code will continue.
                incoming.consumeEach { frame ->
                    // Frames can be [Text], [Binary], [Ping], [Pong], [Close].
                    // We are only interested in textual messages, so we filter it.
                    if (frame is Frame.Text) {
                        println("Received in connect ${frame.readText()}")
                        val connectedUser = mapper.readValue(frame.readText(), UserConnected::class.java)
                        webSocket.join(connectedUser.userId, this)
                    }
                }
            } catch (e: Exception) {
                println("Got error $e")
                e.printStackTrace()
            } finally {
                // TODO...
            }
        }
    }
}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()

