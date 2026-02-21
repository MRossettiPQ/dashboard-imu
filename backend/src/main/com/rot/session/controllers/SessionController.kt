package com.rot.session.controllers

import com.rot.core.jaxrs.ResultContent
import com.rot.session.dtos.ArticulationTypeDto
import com.rot.session.dtos.SessionDto
import com.rot.session.dtos.SessionResponse
import com.rot.session.models.ArticulationType
import com.rot.session.models.Session
import io.quarkus.security.Authenticated
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.jboss.resteasy.reactive.RestPath
import java.util.*

@Authenticated
@ApplicationScoped
@Path("/api/sessions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class SessionController {

    @GET
    @Transactional
    @Path("/metadata")
    @Operation(
        summary = "Metadata for movements",
        description = "Metadata for movements"
    )
    @APIResponse(
        responseCode = "200",
        description = "Metadata for movements"
    )
    @APIResponse(responseCode = "500", description = "Internal server error")
    fun metadata(): Response? {
        val articulationTypes = ArticulationType
            .createQuery()
            .fetch()

        val metadata: MutableMap<String, Any?> = mutableMapOf()
        metadata["procedureTypes"] = articulationTypes.map(ArticulationTypeDto::from)

        return ResultContent.of(metadata).build()
    }

    @POST
    @Transactional
    @Path("/register")
    @Operation(
        summary = "Register a new measurement session",
        description = "Creates a new measurement session and returns its data"
    )
    @APIResponse(
        responseCode = "201",
        description = "Session successfully registered",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = SessionResponse::class),
            )
        ]
    )
    @APIResponse(responseCode = "400", description = "Invalid data for registration")
    @APIResponse(responseCode = "500", description = "Internal server error")
    fun save(@Valid body: SessionDto): Response {
        val session = Session.fromDto(body)
        val isNewBean = session.isNewBean

        return ResultContent.of(session.save())
            .withStatusCode(if (isNewBean) Response.Status.CREATED else Response.Status.OK)
            .transform(SessionDto::from)
            .build()
    }

    @GET
    @Path("/{uuid}")
    @Operation(
        summary = "Retrieve measurement session",
        description = "Retrieve a measurement session and return its data"
    )
    @APIResponse(
        responseCode = "200",
        description = "Measurement session found successfully",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = SessionResponse::class),
            )
        ]
    )
    @APIResponse(responseCode = "404", description = "Measurement session not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    fun retrieve(@RestPath("uuid") uuid: UUID): Response {
        val session = Session.findOrThrowById(uuid, message = "Measurement session not found")
        return ResultContent.of(session)
            .transform(SessionDto::from)
            .build()
    }

}
