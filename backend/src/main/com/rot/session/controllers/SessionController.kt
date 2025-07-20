package com.rot.session.controllers

import com.rot.core.jaxrs.ResultContent
import com.rot.session.dtos.CreateSessionDto
import com.rot.session.dtos.RetrieveSessionDto
import com.rot.session.dtos.SessionResponse
import com.rot.session.models.Session
import com.rot.user.dtos.UserDto
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
import org.jboss.resteasy.reactive.RestQuery
import java.util.*

@Authenticated
@ApplicationScoped
@Path("/api/sessions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class SessionController {

    @POST
    @Transactional
    @Path("/register")
    @Operation(
        summary = "Registrar nova sessão de medição",
        description = "Cria um nova sessão de medição e retorna seus dados"
    )
    @APIResponse(
        responseCode = "201",
        description = "Sessão registrada com sucesso",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = SessionResponse::class),
            )
        ]
    )
    @APIResponse(responseCode = "400", description = "Dados inválidos para registro")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun save(@Valid body: CreateSessionDto): Response {
        var session = Session.fromDto(body)

        session.validate()
        session = session.save()

        return ResultContent.of()
            .withStatusCode(Response.Status.OK)
            .withContent(RetrieveSessionDto.from(session))
            .build()
    }

    @GET
    @Path("/{uuid}")
    @Operation(
        summary = "Resgatar sessão de medição",
        description = "Resgatar sessão de medição e retorna seus dados"
    )
    @APIResponse(
        responseCode = "200",
        description = "Sessão de medição encontrada com sucesso",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = SessionResponse::class),
            )
        ]
    )
    @APIResponse(responseCode = "404", description = "Sessão de medição não encontrada")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun retrieve(@RestPath("uuid") uuid: UUID): Response {
        val session = Session.findOrThrowById(uuid, message = "Sessão de medição não encontrada")
        return ResultContent.of()
            .withContent(RetrieveSessionDto.from(session))
            .build()
    }

    @GET
    @Path("/")
    @Operation(
        summary = "Paginação das sessões de medições realizadas",
        description = "Listar sessões de medições realizadas e retorna seus dados"
    )
    @APIResponse(
        responseCode = "200",
        description = "Paginação das sessões de medições realizadas",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = SessionResponse::class),
            )
        ]
    )
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun list(
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int,
    ): Response {
        val query = Session.createQuery()

        return ResultContent.of()
            .withContent(Session.fetch(query, page, rpp).transform(RetrieveSessionDto::from))
            .build()
    }

}