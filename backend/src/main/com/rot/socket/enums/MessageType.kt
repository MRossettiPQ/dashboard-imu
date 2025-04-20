package com.rot.socket.enums

enum class MessageType(val description: String) {
    RESTART("Restart"),
    START("Start"),
    STOP("Stop"),
    MESSAGE("Message"),
    DEFAULT("Default"),
    REGISTER_USER("Register user"),
    REGISTER_SENSOR("Register sensor"),
}