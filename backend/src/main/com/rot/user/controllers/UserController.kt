package com.rot.user.controllers

import com.rot.core.jaxrs.ResultContent
import com.rot.user.dtos.UserDto
import com.rot.user.models.User
import io.quarkus.security.Authenticated
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery
import java.util.*

@Authenticated
@ApplicationScoped
@Path("/api/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class UserController {

    @GET
    @Path("/{uuid}")
    fun get(@RestPath("uuid") uuid: UUID): Response? {
        val user = User.findOrThrowById(uuid)
        return ResultContent.of().withContent(UserDto.from(user)).build()
    }

    @GET
    @Path("/")
    fun list(
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int,
    ): Response? {
        val query = User.createQuery()

        return ResultContent.of().withContent(User.fetch(query, page, rpp).transform(UserDto::from)).build()
    }

}