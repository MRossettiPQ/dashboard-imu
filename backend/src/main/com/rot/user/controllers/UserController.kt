package com.rot.user.controllers

import com.rot.core.context.ApplicationContext
import com.rot.core.exceptions.ApplicationException
import com.rot.core.jaxrs.ResultContent
import com.rot.user.dtos.UserDto
import com.rot.user.enums.UserRoleString
import com.rot.user.models.User
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery
import java.util.*

@ApplicationScoped
@Path("/api/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(UserRoleString.PHYSIOTHERAPIST, UserRoleString.ADMINISTRATOR)
class UserController {

    @GET
    @Path("/{uuid}")
    fun get(@RestPath("uuid") uuid: UUID): Response {
        val entity = User.findOrThrowById(uuid)
        return ResultContent.of(entity)
            .transform(UserDto::from)
            .build()
    }

    @GET
    @Path("/")
    fun list(
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int
    ): Response {
        val query = User.createQuery()

        return ResultContent.of(User.fetch(query, page, rpp))
            .transform(UserDto::from)
            .build()
    }

    @POST
    @Path("/")
    fun save(body: UserDto): Response {
        val entity = User.fromDto(body)
        val user = ApplicationContext.user

        if(user?.id != entity.id) {
            throw ApplicationException("User not authorized", Response.Status.FORBIDDEN)
        }

        return ResultContent.of(entity)
            .withStatusCode(if(entity.isNewBean) Response.Status.CREATED else Response.Status.OK)
            .transform(UserDto::from)
            .build()
    }

}