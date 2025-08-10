package com.rot.user.controllers

import com.rot.core.context.ApplicationContext
import com.rot.core.exceptions.ApplicationException
import com.rot.core.jaxrs.ResultContent
import com.rot.user.dtos.UserDto
import com.rot.user.dtos.UserPaginationResponse
import com.rot.user.dtos.UserResponse
import com.rot.user.enums.UserRoleString
import com.rot.user.models.User
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
@Path("/api/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(UserRoleString.PHYSIOTHERAPIST, UserRoleString.ADMINISTRATOR)
class UserController {

    @GET
    @Path("/{uuid}")
    @Operation(
        summary = "Retrieve user",
        description = "Retrieve a user by UUID"
    )
    @APIResponse(
        responseCode = "200",
        description = "Returns the registered user",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = UserResponse::class),
            )
        ]
    )
    @APIResponse(responseCode = "403", description = "User not authenticated")
    @APIResponse(responseCode = "500", description = "Internal server error")
    fun get(@RestPath("uuid") uuid: UUID): Response {
        val entity = User.findOrThrowById(uuid)
        return ResultContent.of(entity)
            .transform(UserDto::from)
            .build()
    }

    @GET
    @Path("/")
    @Operation(
        summary = "Retrieve users",
        description = "Retrieve a paginated list of users"
    )
    @APIResponse(
        responseCode = "200",
        description = "Paginated list of users",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = UserPaginationResponse::class),
            )
        ]
    )
    @APIResponse(responseCode = "403", description = "User not authenticated")
    @APIResponse(responseCode = "500", description = "Internal server error")
    fun list(
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int
    ): Response {
        val query = User.createQuery()

        return ResultContent.of(User::class.java, query, page, rpp)
            .transform(UserDto::from)
            .build()
    }

    @POST
    @Path("/")
    @Operation(
        summary = "Update user",
        description = "Update user data"
    )
    @APIResponse(
        responseCode = "200",
        description = "User updated successfully",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = UserResponse::class),
            )
        ]
    )
    @APIResponse(responseCode = "403", description = "User not authenticated")
    @APIResponse(responseCode = "500", description = "Internal server error")
    fun save(body: UserDto): Response {
        val entity = User.fromDto(body)
        val user = ApplicationContext.user

        if(user?.id != entity.id) {
            throw ApplicationException("User not authorized", Response.Status.FORBIDDEN)
        }

        return ResultContent.of(entity.save())
            .withStatusCode(Response.Status.OK)
            .transform(UserDto::from)
            .withMessage("User updated successfully")
            .build()
    }

}