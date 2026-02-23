package com.rot.session.controllers

import com.rot.core.jaxrs.Content
import com.rot.core.jaxrs.Pagination
import com.rot.core.jaxrs.ResultContent
import com.rot.session.dtos.MovementDto
import com.rot.session.models.Movement
import com.rot.session.models.Session
import io.quarkus.security.Authenticated
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery
import org.jboss.resteasy.reactive.RestResponse

@Authenticated
@ApplicationScoped
@Path("/api/articulations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class ArticulationController {

    @GET
    @Transactional
    @Path("/{id}/movements/")
    @Operation(
        summary = "Paginação de movimentações da articulação",
        description = "Lista as movimentações realizadas em uma articulação específica"
    )
    @APIResponse(
        responseCode = "200",
        description = "Paginação de movimentações recuperada com sucesso",
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun listMovements(
        @RestPath("id") id: Int,
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int,
    ): RestResponse<Content<Pagination<MovementDto>>> {
        val query = Movement.createQuery()
            .where(Movement.q.articulation().id.eq(id))

        return ResultContent.of(Session.fetch(query, page, rpp))
            .transform(MovementDto::from)
            .build()
    }

}
