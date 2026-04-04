package com.rot.user.controllers

import com.rot.core.exceptions.ApplicationException
import com.rot.core.jaxrs.Content
import com.rot.core.jaxrs.Pagination
import com.rot.core.jaxrs.ResultContent
import com.rot.session.dtos.SessionRead
import com.rot.session.models.Session
import com.rot.user.dtos.PatientDto
import com.rot.user.enums.UserRole
import com.rot.user.enums.UserRoleString
import com.rot.user.models.Patient
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery
import org.jboss.resteasy.reactive.RestResponse
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
        summary = "Obter um paciente",
        description = "Recupera um paciente através do seu UUID"
    )
    @APIResponse(
        responseCode = "200",
        description = "Paciente encontrado e retornado com sucesso",
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "404", description = "Paciente não encontrado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun retrieve(@RestPath("uuid") uuid: UUID): RestResponse<Content<PatientDto>> {
        val entity = Patient.findOrThrowById(uuid)
        return ResultContent.of(entity)
            .transform(PatientDto::from)
            .build()
    }

    @GET
    @Transactional
    @Path("/")
    @Operation(
        summary = "Listar pacientes",
        description = "Retorna uma lista paginada de pacientes"
    )
    @APIResponse(
        responseCode = "200",
        description = "Lista paginada de pacientes recuperada com sucesso",
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun list(
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int
    ): RestResponse<Content<Pagination<PatientDto>>> {
        val query = Patient.createQuery()

        return ResultContent.of(Patient.fetch(query, page, rpp))
            .transform(PatientDto::from)
            .build()
    }

    @GET
    @Transactional
    @Path("/{uuid}/sessions/")
    @Operation(
        summary = "Paginação de sessões de medição realizadas",
        description = "Lista as sessões de medição realizadas de um paciente específico"
    )
    @APIResponse(
        responseCode = "200",
        description = "Paginação de sessões de medição realizada com sucesso",
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "404", description = "Paciente não encontrado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun listSessions(
        @RestPath("uuid") uuid: UUID,
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int,
    ): RestResponse<Content<Pagination<SessionRead>>> {
        Patient.findOrThrowById(uuid)

        val query = Session.createQuery()
            .where(Session.q.patient().id.eq(uuid))

        return ResultContent.of(Session.fetch(query, page, rpp))
            .transform(SessionRead::from)
            .build()
    }

    @POST
    @Transactional
    @Path("/")
    @Operation(
        summary = "Salvar paciente",
        description = "Salva um novo paciente ou atualiza um já existente"
    )
    @APIResponse(
        responseCode = "200",
        description = "Paciente salvo com sucesso",
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun save(body: PatientDto): RestResponse<Content<PatientDto>> {
        val entity = Patient.fromDto(body)
        val isNewBean = entity.isNewBean

        val user = entity.user
        if(user != null && isNewBean) {
            user.encryptAndSetPassword(body.user?.password!!)
            user.role = UserRole.USER
            entity.user = user.save()
        }

        try {
            entity.validate()
        } catch (e: ApplicationException) {
            e.printStackTrace() // This will reveal the actual business error
            throw e
        }
        entity.active = true
        return ResultContent.of(entity.save())
            .withStatusCode(Response.Status.OK)
            .transform(PatientDto::from)
            .withMessage("Paciente salvo com sucesso")
            .build()
    }

}