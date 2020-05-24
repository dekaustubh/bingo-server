package com.dekaustubh.socket

enum class MessageType {
    MATCH_JOIN,
    MATCH_CREATE,
    MATCH_START,
    MATCH_LEFT,
    MATCH_TURN,
    MATCH_WON,
    HEARTBEAT,
    CONNECT
}