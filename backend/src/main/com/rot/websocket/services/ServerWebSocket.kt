package com.rot.websocket.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.websocket.OnClose
import jakarta.websocket.OnError
import jakarta.websocket.OnMessage
import jakarta.websocket.OnOpen
import jakarta.websocket.Session
import jakarta.websocket.server.PathParam
import jakarta.websocket.server.ServerEndpoint

@ApplicationScoped
@ServerEndpoint("/api/websocket/{name}")
class ServerWebSocket {
    @OnOpen
    fun onOpen(session: Session?, @PathParam("name") name: String) {
        println("onOpen - $name")
    }

    @OnClose
    fun onClose(session: Session?, @PathParam("name") name: String) {
        println("onClose - $name")
    }

    @OnError
    fun onError(session: Session?, @PathParam("name") name: String, throwable: Throwable) {
        println("onError - $name: $throwable")
    }

    @OnMessage
    fun onMessage(message: String, @PathParam("name") name: String) {
        println("onMessage - $name: $message")
    }
}