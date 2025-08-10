package com.rot.access.controllers

import com.rot.access.dtos.LoginDto
import com.rot.access.dtos.RegisterDto
import com.rot.core.config.ApplicationConfig
import com.rot.core.context.ApplicationContext
import com.rot.core.exceptions.ApplicationException
import com.rot.core.jaxrs.ResultContent
import com.rot.core.utils.EncryptUtils
import com.rot.user.dtos.UserDto
import com.rot.user.dtos.UserResponse
import com.rot.user.models.User
import io.quarkus.security.Authenticated
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse

@ApplicationScoped
@Path("/api/access")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class AccessController(
    private val applicationConfig: ApplicationConfig
) {

    @POST
    @Transactional
    @Path("/register")
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user in the system and returns their data"
    )
    @APIResponse(
        responseCode = "201",
        description = "User successfully registered",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = UserResponse::class),
            )
        ]
    )
    @APIResponse(responseCode = "401", description = "Incorrect password")
    @APIResponse(responseCode = "403", description = "User not authenticated")
    @APIResponse(responseCode = "404", description = "User not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    fun register(@Valid body: RegisterDto): Response {
        var user = User.fromDto(body)
        user.active = true
        user.encryptAndSetPassword(body.password)
        user.validate()
        user = user.save()
        user.generateToken(
            issuer = applicationConfig.security().issuer(),
            subject = applicationConfig.security().subject()
        )
        return ResultContent.of(user)
            .transform { UserDto.from(it, true) }
            .withStatusCode(Response.Status.CREATED)
            .withMessage("User registered successfully")
            .build()
    }

    @POST
    @Transactional
    @Path("/login")
    @Operation(
        summary = "User login",
        description = "Authenticates a user and returns their data"
    )
    @APIResponse(
        responseCode = "200",
        description = "User successfully authenticated",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = UserResponse::class),
            )
        ]
    )
    @APIResponse(responseCode = "401", description = "Incorrect password")
    @APIResponse(responseCode = "403", description = "User not authenticated")
    @APIResponse(responseCode = "404", description = "User not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    fun login(@Valid body: LoginDto): Response {
        val user = User.findByUsername(body.username)

        val password = EncryptUtils.generateHash(body.password, user.salt!!)
        if (user.password != password) {
            throw ApplicationException("Passwords do not match", Response.Status.UNAUTHORIZED)
        }

        user.generateToken(
            issuer = applicationConfig.security().issuer(),
            subject = applicationConfig.security().subject()
        )
        return ResultContent.of(user)
            .transform { UserDto.from(it, true) }
            .build()
    }

    @GET
    @Authenticated
    @Path("/context")
    @Operation(
        summary = "Authenticated user context",
        description = "Returns the data of the currently authenticated user"
    )
    @APIResponse(
        responseCode = "200",
        description = "User authenticated",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = UserResponse::class),
            )
        ]
    )
    @APIResponse(responseCode = "401", description = "Invalid authentication")
    @APIResponse(responseCode = "403", description = "User not authenticated")
    @APIResponse(responseCode = "404", description = "User not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    fun context(): Response {
        val user = ApplicationContext.user
            ?: throw ApplicationException("User not authenticated", Response.Status.FORBIDDEN)
        return ResultContent.of(user)
            .transform(UserDto::from)
            .build()
    }

}
