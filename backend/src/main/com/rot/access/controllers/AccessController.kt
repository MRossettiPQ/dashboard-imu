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
        summary = "Registrar novo usuário",
        description = "Cria um novo usuário no sistema e retorna seus dados"
    )
    @APIResponse(
        responseCode = "201",
        description = "Usuário registrado com sucesso",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = UserResponse::class),
            )
        ]
    )
    @APIResponse(responseCode = "400", description = "Dados inválidos para registro")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun register(@Valid body: RegisterDto): Response {
        var user = User.fromDto(body)
        user.active = true
        user.encryptAndSetPassword(body.password)
        user.validate()
        user = user.save()
        user.generateToken(applicationConfig.security().issuer(), applicationConfig.security().subject())

        return ResultContent.of().withContent(UserDto.from(user, true)).build()
    }

    @POST
    @Transactional
    @Path("/login")
    @Operation(
        summary = "Login do usuário",
        description = "Autentica um usuário e retorna os dados"
    )
    @APIResponse(
        responseCode = "200",
        description = "Usuário autenticado",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = UserResponse::class),
            )
        ]
    )
    @APIResponse(
        responseCode = "404",
        description = "Usuário não encontrado"
    )
    @APIResponse(
        responseCode = "401",
        description = "Senha incorreta"
    )
    fun login(@Valid body: LoginDto): Response {
        val user = User.findByUsername(body.username)

        val password = EncryptUtils.generateHash(body.password, user.salt!!)
        if (user.password != password) {
            throw ApplicationException("Passwords do not match", Response.Status.UNAUTHORIZED)
        }

        user.generateToken(applicationConfig.security().issuer(), applicationConfig.security().subject())
        return ResultContent.of().withContent(UserDto.from(user, true)).build()
    }

    @GET
    @Authenticated
    @Path("/context")
    @Operation(
        summary = "Contexto do usuário",
        description = "Retorna os dados do usuário autenticado"
    )
    @APIResponse(
        responseCode = "200",
        description = "Usuário autenticado",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = UserResponse::class),
            )
        ]
    )
    @APIResponse(
        responseCode = "403",
        description = "Usuário não autenticado"
    )
    fun context(): Response {
        val user = ApplicationContext.user
            ?: throw ApplicationException("User not authenticated", Response.Status.FORBIDDEN)
        return ResultContent.of().withContent(UserDto.from(user)).build()
    }

}