package com.rot.development.controllers

import com.rot.core.config.ApplicationConfig
import com.rot.core.jaxrs.ResultContent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDateTime

@Path("/api/core")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class DevelopmentController(
    private val applicationConfig: ApplicationConfig
) {

    @GET
    @Path("/ping")
    fun ping(): Response {
        val metadata = mutableMapOf<String, Any?>()
        metadata["name"] = applicationConfig.name()
        metadata["environment"] = applicationConfig.environment()
        metadata["dateTime"] = LocalDateTime.now()

        return ResultContent.of().withContent(metadata).build()
    }

}