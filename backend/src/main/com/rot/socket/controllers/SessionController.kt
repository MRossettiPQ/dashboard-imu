package com.rot.socket.controllers

import com.rot.core.jaxrs.ResultContent
import com.rot.socket.services.SocketService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Response

@ApplicationScoped
@Path("/api/sessions")
class SessionController(
    val socketService: SocketService
) {
    @GET
    @Path("/")
    fun list(): Response {
        val metadata = mutableMapOf<String, Any?>()
        metadata["sessions"] = socketService.sessions

        return ResultContent.of(metadata)
            .build()
    }
}