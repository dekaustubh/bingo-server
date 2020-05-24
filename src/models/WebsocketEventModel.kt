package com.dekaustubh.models

import com.dekaustubh.models.User.User
import com.dekaustubh.socket.MessageType
import com.fasterxml.jackson.annotation.JsonProperty

sealed class WebsocketEvent(
    @JsonProperty("message_type")
    val messageType: MessageType
)

data class UserConnected(
    @JsonProperty("user_id")
    val userId: String,
    @JsonProperty("message_type")
    val messageType: MessageType
)

data class MatchJoined(
    @JsonProperty("user_id")
    val userId: String,
    @JsonProperty("user_name")
    val userName: String,
    @JsonProperty("match_id")
    val matchId: Long,
    @JsonProperty("room_id")
    val roomId: Long
) : WebsocketEvent(MessageType.MATCH_JOIN)

data class MatchLeft(
    @JsonProperty("user_id")
    val userId: String,
    @JsonProperty("user_name")
    val userName: String,
    @JsonProperty("match_id")
    val matchId: Long,
    @JsonProperty("room_id")
    val roomId: Long
) : WebsocketEvent(MessageType.MATCH_LEFT)

data class MatchCreated(
    @JsonProperty("user_id")
    val userId: String,
    @JsonProperty("user_name")
    val userName: String,
    @JsonProperty("match_id")
    val matchId: Long,
    @JsonProperty("room_id")
    val roomId: Long
) : WebsocketEvent(MessageType.MATCH_CREATE)

data class MatchStarted(
    @JsonProperty("user_id")
    val userId: String,
    @JsonProperty("user_name")
    val userName: String,
    @JsonProperty("match_id")
    val matchId: Long,
    @JsonProperty("room_id")
    val roomId: Long
) : WebsocketEvent(MessageType.MATCH_START)

data class MatchTurn(
    @JsonProperty("user_id")
    val userId: String,
    @JsonProperty("user_name")
    val userName: String,
    @JsonProperty("match_id")
    val matchId: Long,
    @JsonProperty("room_id")
    val roomId: Long,
    @JsonProperty("next_turn")
    val nextTurn: User?,
    val number: Int
) : WebsocketEvent(MessageType.MATCH_TURN)

data class MatchWon(
    @JsonProperty("user_id")
    val userId: String,
    val points: Int,
    @JsonProperty("match_id")
    val matchId: Long,
    @JsonProperty("room_id")
    val roomId: Long
) : WebsocketEvent(MessageType.MATCH_WON)