package com.rot.user.controllers

import com.rot.core.jaxrs.ResultContent
import com.rot.user.dtos.PatientDto
import com.rot.user.enums.UserRoleString
import com.rot.user.models.Patient
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery
import java.util.*

@ApplicationScoped
@Path("/api/patients")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(UserRoleString.PHYSIOTHERAPIST, UserRoleString.ADMINISTRATOR)
class PatientController {

    @GET
    @Path("/{uuid}")
    fun get(@RestPath("uuid") uuid: UUID): Response {
        val entity = Patient.findOrThrowById(uuid)
        return ResultContent.of()
            .withContent(PatientDto.from(entity))
            .build()
    }

    @GET
    @Path("/")
    fun list(@DefaultValue("1") @RestQuery page: Int, @DefaultValue("10") @RestQuery rpp: Int): Response {
        val query = Patient.createQuery()

        return ResultContent.of()
            .withContent(Patient.fetch(query, page, rpp).transform(PatientDto::from))
            .build()
    }

    @POST
    @Path("/")
    fun save(body: PatientDto): Response {
        var entity = Patient.fromDto(body)
        entity.active = true
        entity.validate()

        entity = entity.save()
        return ResultContent.of()
            .withContent(entity.toDto())
            .build()
    }

}