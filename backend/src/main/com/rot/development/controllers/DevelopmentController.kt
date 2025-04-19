package com.rot.development.controllers

import com.rot.core.config.ApplicationConfig
import com.rot.core.jaxrs.ResultContent
import com.rot.user.models.User
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDateTime
import java.util.*

@Path("/core")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
class DevelopmentController(
    private val applicationConfig: ApplicationConfig
) {

    @GET
    @Path("/ping")
    fun ping(): Response? {
        val metadata = mutableMapOf<String, Any>()
        metadata["dateTime"] = LocalDateTime.now()
        metadata["name"] = applicationConfig.name()
        metadata["environment"] = applicationConfig.environment()

        val user = User.createQuery()
            .from(User.q)
            .select(User.q)
            .fetch()

        println(user)
        metadata["user"] = user

        return ResultContent.of().withContent(metadata).build()
    }

}