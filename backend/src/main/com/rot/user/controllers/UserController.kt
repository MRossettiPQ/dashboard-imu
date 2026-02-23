package com.rot.user.controllers

import com.rot.core.context.ApplicationContext
import com.rot.core.exceptions.ApplicationException
import com.rot.core.jaxrs.Content
import com.rot.core.jaxrs.Pagination
import com.rot.core.jaxrs.ResultContent
import com.rot.user.dtos.UserDto
import com.rot.user.enums.UserRoleString
import com.rot.user.models.User
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
@Path("/api/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(UserRoleString.PHYSIOTHERAPIST, UserRoleString.ADMINISTRATOR)
class UserController {

    @GET
    @Transactional
    @Path("/{uuid}")
    @Operation(
        summary = "Obter um usuário",
        description = "Recupera um usuário através do seu UUID"
    )
    @APIResponse(
        responseCode = "200",
        description = "Usuário encontrado e retornado com sucesso",
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "404", description = "Usuário não encontrado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun get(@RestPath("uuid") uuid: UUID): RestResponse<Content<UserDto>> {
        val entity = User.findOrThrowById(uuid)
        return ResultContent.of(entity)
            .transform(UserDto::from)
            .build()
    }

    @GET
    @Transactional
    @Path("/")
    @Operation(
        summary = "Listar usuários",
        description = "Retorna uma lista paginada de usuários"
    )
    @APIResponse(
        responseCode = "200",
        description = "Lista paginada de usuários recuperada com sucesso",
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun list(
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int
    ): RestResponse<Content<Pagination<UserDto>>> {
        val query = User.createQuery()

        return ResultContent.of(User::class.java, query, page, rpp)
            .transform(UserDto::from)
            .build()
    }

    @POST
    @Transactional
    @Path("/")
    @Operation(
        summary = "Atualizar usuário",
        description = "Atualiza os dados de um usuário existente"
    )
    @APIResponse(
        responseCode = "200",
        description = "Usuário atualizado com sucesso",
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autorizado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun save(body: UserDto): RestResponse<Content<UserDto>> {
        val entity = User.fromDto(body)
        val user = ApplicationContext.user

        if(user?.id != entity.id) {
            throw ApplicationException("Usuário não autorizado para esta alteração", Response.Status.FORBIDDEN)
        }

        return ResultContent.of(entity.save())
            .withStatusCode(Response.Status.OK)
            .transform(UserDto::from)
            .withMessage("Usuário atualizado com sucesso")
            .build()
    }

}