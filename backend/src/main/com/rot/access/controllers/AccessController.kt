package com.rot.access.controllers

import com.rot.access.dtos.LoginDto
import com.rot.access.dtos.RegisterDto
import com.rot.core.context.ApplicationContext
import com.rot.core.exceptions.ApplicationException
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

@ApplicationScoped
@Path("/api/access")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class AccessController {

    @POST
    @Transactional
    @Path("/register")
    fun register(@Valid body: RegisterDto): Response? {
        val user = User()
        user.username = body.username
        user.name = body.name
        user.email = body.email
        user.encryptAndSetPassword(body.password!!)

        return ResultContent.of().withContent(UserDto.from(user.save(), true)).build()
    }

    @POST
    @Transactional
    @Path("/login")
    fun login(@Valid body: LoginDto): Response? {
        val user = User.createQuery()
            .where(User.q.username.eq(body.username))
            .fetchFirst() ?: throw ApplicationException("User not found", 404)

        val password = EncryptUtils.generateHash(body.password!!, user.salt!!)
        if (user.password != password) {
            throw ApplicationException("Passwords do not match", 401)
        }

        return ResultContent.of().withContent(UserDto.from(user, true)).build()
    }

    @GET
    @Authenticated
    @Path("/context")
    fun context(): Response? {
        val user = ApplicationContext.user
            ?: throw ApplicationException("User not authenticated", 403)
        return ResultContent.of().withContent(UserDto.from(user)).build()
    }

}