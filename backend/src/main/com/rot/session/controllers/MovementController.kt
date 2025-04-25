package com.rot.session.controllers

import com.rot.core.jaxrs.ResultContent
import com.rot.session.dtos.MovementDto
import com.rot.session.models.Movement
import com.rot.session.services.SciLabServices
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery
import java.util.*

@ApplicationScoped
@Path("/api/movements")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class MovementController(
    private val sciLabServices: SciLabServices
) {

    @GET
    @Path("/{uuid}/calculate-variability")
    fun calculateVariability(@RestPath("uuid") uuid: UUID): Response? {
        val movement = Movement.findOrThrowById(uuid)
        val result = sciLabServices.calculateVariabilityCenter(movement)
        return ResultContent.of().withContent(result).build()
    }

    @GET
    @Path("/{uuid}")
    fun get(@RestPath("uuid") uuid: UUID): Response? {
        val session = Movement.findOrThrowById(uuid)
        return ResultContent.of().withContent(MovementDto.from(session)).build()
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