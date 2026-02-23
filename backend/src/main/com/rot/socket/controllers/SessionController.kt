package com.rot.socket.controllers

import com.rot.core.jaxrs.Content
import com.rot.core.jaxrs.ResultContent
import com.rot.socket.services.SocketService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import org.jboss.resteasy.reactive.RestResponse

@ApplicationScoped
@Path("/api/sessions")
class SessionController(
    val socketService: SocketService
) {
    @GET
    @Path("/")
    fun list(): RestResponse<Content<MutableMap<String, Any?>>> {
        val metadata = mutableMapOf<String, Any?>()
        metadata["sessions"] = socketService.sessions

        return ResultContent.of(metadata)
            .build()
    }
}