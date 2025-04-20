package com.rot.session.controllers

import com.rot.core.jaxrs.ResultContent
import com.rot.session.dtos.SessionDto
import com.rot.session.models.Session
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery
import java.util.*

@ApplicationScoped
@Path("/api/sessions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class SessionController {

    @GET
    @Path("/{uuid}")
    fun get(@RestPath("uuid") uuid: UUID): Response? {
        val session = Session.findOrThrowById(uuid)
        return ResultContent.of().withContent(SessionDto.from(session)).build()
    }

    @GET
    @Path("/")
    fun list(
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int,
    ): Response? {
        val metadata = mutableMapOf<String, Any>()
        return ResultContent.of().withContent(metadata).build()
    }

}