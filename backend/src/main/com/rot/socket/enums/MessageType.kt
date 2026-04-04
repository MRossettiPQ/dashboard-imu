package com.rot.socket.enums

object SocketEvents {
    // All
    const val WELCOME = "WELCOME"
}

enum class OriginType(val description: String) {
    BACKEND("backend"),
    FRONTEND("frontend"),
    SENSOR("sensor"),
    UNKNOWN("unknown");
}

enum class MessageType(val description: String) {
    DEFAULT("Default"),
    // All
    WELCOME(SocketEvents.WELCOME);
}