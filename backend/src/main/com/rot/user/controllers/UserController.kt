package com.rot.user.controllers

import com.rot.core.jaxrs.Pagination
import com.rot.core.jaxrs.ResultContent
import com.rot.user.dtos.UserDto
import com.rot.user.enums.UserRole
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
        return ResultContent.of()
            .withContent(UserDto.from(entity))
            .build()
    }

    @GET
    @Path("/")
    fun list(@DefaultValue("1") @RestQuery page: Int, @DefaultValue("10") @RestQuery rpp: Int): Response {
        val query = User.createQuery()

        return ResultContent.of()
            .withContent(User.fetch(query, page, rpp).transform(UserDto::from))
            .build()
    }

    @POST
    @Path("/")
    fun save(body: UserDto): Response {
        var entity = User.fromDto(body)
        entity.role = UserRole.USER
        entity.active = true
        entity.validate()

        entity = entity.save()
        return ResultContent.of()
            .withContent(entity.toDto())
            .build()
    }

}