package com.dekaustubh.models

import com.dekaustubh.socket.MessageType
import com.fasterxml.jackson.annotation.JsonProperty

sealed class WebsocketEvent(
    @JsonProperty("message_type")
    val messageType: MessageType
)

data class UserConnected(
    @JsonProperty("user_id")
    val userId: Long
): WebsocketEvent(MessageType.CONNECT)

data class UserJoined(
    @JsonProperty("user_id")
    val userId: Long,
    @JsonProperty("user_name")
    val userName: String,
    @JsonProperty("match_id")
    val matchId: Long
) : WebsocketEvent(MessageType.JOIN)

data class MatchStarted(
    @JsonProperty("user_id")
    val userId: Long,
    @JsonProperty("user_name")
    val userName: String,
    @JsonProperty("match_id")
    val matchId: Long
) : WebsocketEvent(MessageType.START)

data class TurnTaken(
    @JsonProperty("user_id")
    val userId: Long,
    @JsonProperty("user_name")
    val userName: String,
    @JsonProperty("match_id")
    val matchId: Long,
    @JsonProperty("next_turn")
    val nextTurn: Long,
    val number: Int
) : WebsocketEvent(MessageType.TAKE_TURN)

data class MatchWon(
    @JsonProperty("user_id")
    val userId: Long,
    val points: Int,
    @JsonProperty("room_id")
    val roomId: Long
) : WebsocketEvent(MessageType.WIN)