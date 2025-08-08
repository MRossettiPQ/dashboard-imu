package com.rot.user.controllers

import com.rot.core.jaxrs.ResultContent
import com.rot.user.dtos.PatientDto
import com.rot.user.dtos.PatientPaginationResponse
import com.rot.user.dtos.PatientResponse
import com.rot.user.enums.UserRoleString
import com.rot.user.models.Patient
import jakarta.annotation.security.RolesAllowed
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

@ApplicationScoped
@Path("/api/patients")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(UserRoleString.PHYSIOTHERAPIST, UserRoleString.ADMINISTRATOR)
class PatientController {

    @GET
    @Path("/{uuid}")
    @Operation(
        summary = "Resgatar um paciente",
        description = "Resgatar um paciente com base no seu UUID"
    )
    @APIResponse(
        responseCode = "200",
        description = "Resgatar o paciente cadastrado",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = PatientResponse::class),
            )
        ]
    )
    @APIResponse(
        responseCode = "403",
        description = "Usuário não autenticado"
    )
    fun get(@RestPath("uuid") uuid: UUID): Response {
        val entity = Patient.findOrThrowById(uuid)
        return ResultContent.of(entity)
            .transform(PatientDto::from)
            .build()
    }

    @GET
    @Path("/")
    @Operation(
        summary = "Resgatar pacientes paginados",
        description = "Resgatar pacientes paginados"
    )
    @APIResponse(
        responseCode = "200",
        description = "Resgatar pacientes paginados",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = PatientPaginationResponse::class),
            )
        ]
    )
    @APIResponse(
        responseCode = "403",
        description = "Usuário não autenticado"
    )
    fun list(
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int
    ): Response {
        val query = Patient.createQuery()

        return ResultContent.of(Patient.fetch(query, page, rpp))
            .transform(PatientDto::from)
            .build()
    }

    @POST
    @Path("/")
    @Operation(
        summary = "Salvar paciente",
        description = "Salvar um novo paciente ou atualizar um existente"
    )
    @APIResponse(
        responseCode = "200",
        description = "Paciente salvo com sucesso",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = PatientResponse::class),
            )
        ]
    )
    @APIResponse(
        responseCode = "403",
        description = "Usuário não autenticado"
    )
    fun save(body: PatientDto): Response {
        val entity = Patient.fromDto(body)
        entity.active = true
        return ResultContent.of(entity.save())
            .transform(PatientDto::from)
            .build()
    }

}