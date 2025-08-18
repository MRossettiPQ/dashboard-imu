package com.rot.development.controllers

import com.rot.core.config.ApplicationConfig
import com.rot.core.jaxrs.ResultContent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Response
import java.time.LocalDateTime

@ApplicationScoped
@Path("/api/core")
class DevelopmentController(
    val config: ApplicationConfig
) {
    @GET
    @Path("/ping")
    fun ping(): Response {
        val metadata = mutableMapOf<String, Any?>()
        metadata["name"] = config.name()
        metadata["environment"] = config.environment()
        metadata["dateTime"] = LocalDateTime.now()

        return ResultContent.of(metadata)
            .withMessage("Ping server test successful")
            .build()
    }
}