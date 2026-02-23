package com.rot.gonimetry.controllers

import com.rot.core.jaxrs.Content
import com.rot.core.jaxrs.Pagination
import com.rot.core.jaxrs.ResultContent
import com.rot.gonimetry.dtos.ArticulationTypeCreateOrUpgradeDto
import com.rot.gonimetry.dtos.ArticulationTypeDto
import com.rot.gonimetry.models.ArticulationType
import com.rot.session.models.Session
import com.rot.session.services.ArticulationTypeService
import io.quarkus.security.Authenticated
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.jboss.resteasy.reactive.RestQuery
import org.jboss.resteasy.reactive.RestResponse

@Authenticated
@ApplicationScoped
@Path("/api/articulation-types")
class ArticulationTypeController(
    val articulationTypeService: ArticulationTypeService
) {

    @POST
    @Transactional
    @Path("/")
    @Operation(
        summary = "Salvar tipo de articulação",
        description = "Cria um novo tipo de articulação ou atualiza um existente"
    )
    @APIResponse(
        responseCode = "200",
        description = "Tipo de articulação salvo com sucesso"
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun save(@Valid body: ArticulationTypeCreateOrUpgradeDto): RestResponse<Content<ArticulationTypeDto>> {
        val articulationType = articulationTypeService.save(body)

        return ResultContent.of(articulationType)
            .transform(ArticulationTypeDto::from)
            .build()
    }

    @GET
    @Transactional
    @Path("/")
    @Operation(
        summary = "Listar tipos de articulação",
        description = "Retorna uma lista paginada de todos os tipos de articulação cadastrados"
    )
    @APIResponse(
        responseCode = "200",
        description = "Lista de tipos de articulação recuperada com sucesso"
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun list(
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int,
    ): RestResponse<Content<Pagination<ArticulationTypeDto>>> {
        val query = ArticulationType.createQuery()

        return ResultContent.of(Session.fetch(query, page, rpp))
            .transform(ArticulationTypeDto::from)
            .build()
    }
}