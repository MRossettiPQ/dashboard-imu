package com.rot.session.controllers

import com.rot.core.jaxrs.Content
import com.rot.core.jaxrs.ResultContent
import com.rot.gonimetry.dtos.ArticulationTypeDto
import com.rot.gonimetry.models.ArticulationType
import com.rot.session.dtos.SessionDto
import com.rot.session.models.Session
import io.quarkus.security.Authenticated
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestResponse
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
        summary = "Metadados para movimentos",
        description = "Retorna os metadados necessários para a configuração de movimentos"
    )
    @APIResponse(
        responseCode = "200",
        description = "Metadados recuperados com sucesso"
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun metadata(): RestResponse<Content<MutableMap<String, Any?>>> {
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
        summary = "Registrar uma nova sessão de medição",
        description = "Cria uma nova sessão de medição e retorna os seus dados"
    )
    @APIResponse(
        responseCode = "200",
        description = "Sessão registrada com sucesso",
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun save(@Valid body: SessionDto): RestResponse<Content<SessionDto>> {
        val session = Session.fromDto(body)
        return ResultContent.of(session.save())
            .withStatusCode(Response.Status.OK)
            .transform(SessionDto::from)
            .build()
    }

    @GET
    @Transactional
    @Path("/{uuid}")
    @Operation(
        summary = "Obter sessão de medição",
        description = "Recupera uma sessão de medição através do seu UUID"
    )
    @APIResponse(
        responseCode = "200",
        description = "Sessão de medição encontrada com sucesso",
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "404", description = "Sessão de medição não encontrada")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun retrieve(@RestPath("uuid") uuid: UUID): RestResponse<Content<SessionDto>> {
        val session = Session.findOrThrowById(uuid, message = "Sessão de medição não encontrada")
        return ResultContent.of(session)
            .transform(SessionDto::from)
            .build()
    }

}
