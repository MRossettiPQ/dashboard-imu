package com.rot.session.controllers

import com.rot.core.jaxrs.ResultContent
import com.rot.session.dtos.ArticulationTypeCreateOrUpgradeDto
import com.rot.session.dtos.ArticulationTypeDto
import com.rot.session.dtos.MovementDto
import com.rot.session.models.ArticulationType
import com.rot.session.models.Movement
import com.rot.session.models.Session
import com.rot.session.services.ArticulationTypeService
import io.quarkus.security.Authenticated
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery
import java.util.*

@Authenticated
@ApplicationScoped
@Path("/api/articulation-types")
class ArticulationTypeController(
    val articulationTypeService: ArticulationTypeService
) {

    @POST
    @Path("/")
    @Transactional
    fun save(@Valid body: ArticulationTypeCreateOrUpgradeDto): Response {
        val articulationType = articulationTypeService.save(body)

        return ResultContent.of(articulationType)
            .transform(ArticulationTypeDto::from)
            .build()
    }

    fun list(
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int,
    ): Response {
        val query = ArticulationType.createQuery()

        return ResultContent.of(Session.fetch(query, page, rpp))
            .transform(ArticulationTypeDto::from)
            .build()
    }


}
