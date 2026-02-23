package com.rot.access.controllers

import com.rot.access.dtos.LoginDto
import com.rot.access.dtos.RefreshTokenDto
import com.rot.access.dtos.RegisterDto
import com.rot.core.config.ApplicationConfig
import com.rot.core.context.ApplicationContext
import com.rot.core.exceptions.ApplicationException
import com.rot.core.jaxrs.Content
import com.rot.core.jaxrs.ResultContent
import com.rot.core.utils.EncryptUtils
import com.rot.user.dtos.UserDto
import com.rot.user.models.User
import io.quarkus.security.Authenticated
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.jboss.resteasy.reactive.RestResponse

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
        summary = "Registrar um novo usuário",
        description = "Cria um novo usuário no sistema e retorna os seus dados"
    )
    @APIResponse(
        responseCode = "200",
        description = "Usuário registrado com sucesso",
    )
    @APIResponse(responseCode = "400", description = "Requisição inválida")
    @APIResponse(responseCode = "401", description = "Senha incorreta")
    @APIResponse(responseCode = "403", description = "Usuário não autenticado")
    @APIResponse(responseCode = "404", description = "Usuário não encontrado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun register(@Valid body: RegisterDto): RestResponse<Content<UserDto>> {
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
            .withStatusCode(Response.Status.OK)
            .withMessage("Usuário registrado com sucesso")
            .build()
    }

    @POST
    @Transactional
    @Path("/login")
    @Operation(
        summary = "Login de usuário",
        description = "Autentica um usuário e retorna os seus dados"
    )
    @APIResponse(
        responseCode = "200",
        description = "Usuário autenticado com sucesso",
    )
    @APIResponse(responseCode = "400", description = "Requisição inválida")
    @APIResponse(responseCode = "401", description = "Senha incorreta")
    @APIResponse(responseCode = "403", description = "Usuário não autenticado")
    @APIResponse(responseCode = "404", description = "Usuário não encontrado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun login(@Valid body: LoginDto): RestResponse<Content<UserDto>> {
        val user = User.findByUsername(body.username)

        val password = EncryptUtils.generateHash(body.password, user.salt!!)
        if (user.password != password) {
            throw ApplicationException("Senha incorreta", Response.Status.UNAUTHORIZED)
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
    @Transactional
    @Path("/context")
    @Operation(
        summary = "Contexto do usuário autenticado",
        description = "Retorna os dados do usuário atualmente autenticado"
    )
    @APIResponse(
        responseCode = "200",
        description = "Usuário autenticado com sucesso",
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "404", description = "Usuário não encontrado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun context(): RestResponse<Content<UserDto>> {
        val user = ApplicationContext.user
            ?: throw ApplicationException("Acesso negado ou usuário não autenticado", Response.Status.FORBIDDEN)
        return ResultContent.of(user)
            .transform(UserDto::from)
            .build()
    }

    @POST
    @Transactional
    @Path("/refresh")
    @Operation(
        summary = "Atualizar token de acesso",
        description = "Gera um novo par de tokens (acesso e refresh) utilizando um refresh token válido"
    )
    @APIResponse(
        responseCode = "200",
        description = "Token atualizado com sucesso",
    )
    @APIResponse(responseCode = "400", description = "Requisição inválida (token ausente)")
    @APIResponse(responseCode = "401", description = "Refresh token inválido ou expirado")
    @APIResponse(responseCode = "404", description = "Usuário não encontrado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun refresh(@Valid body: RefreshTokenDto): RestResponse<Content<UserDto>> {
        val issuer = applicationConfig.security().issuer()
        val subject = applicationConfig.security().subject()

        val user = User.findByRefreshToken(body.refreshToken, issuer)

        user.generateToken(
            issuer = issuer,
            subject = subject
        )
        return ResultContent.of(user)
            .transform { UserDto.from(it, true) }
            .withStatusCode(Response.Status.OK)
            .withMessage("Token atualizado com sucesso")
            .build()
    }

}
