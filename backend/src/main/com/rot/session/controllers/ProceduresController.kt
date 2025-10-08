package com.rot.session.controllers

import com.rot.core.jaxrs.ResultContent
import com.rot.session.dtos.MovementDto
import com.rot.session.dtos.MovementPaginationResponse
import com.rot.session.models.Movement
import com.rot.session.models.Session
import io.quarkus.security.Authenticated
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery
import java.util.*

@Authenticated
@ApplicationScoped
@Path("/api/procedures")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class ProceduresController {

    @GET
    @Path("/{uuid}/movements/")
    @Operation(
        summary = "Pagination of procedure movements",
        description = "List of procedure movements"
    )
    @APIResponse(
        responseCode = "200",
        description = "Pagination of procedure movements",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = MovementPaginationResponse::class),
            )
        ]
    )
    @APIResponse(responseCode = "500", description = "Internal server error")
    fun listMovements(
        @RestPath("uuid") uuid: UUID,
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int,
    ): Response {
        val query = Movement.createQuery()
            .where(Movement.q.procedure().id.eq(uuid))

        return ResultContent.of(Session.fetch(query, page, rpp))
            .transform(MovementDto::from)
            .build()
    }

}
