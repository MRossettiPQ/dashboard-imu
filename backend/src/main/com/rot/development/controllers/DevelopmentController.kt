package com.rot.development.controllers

import com.rot.core.config.ApplicationConfig
import com.rot.core.jaxrs.Content
import com.rot.core.jaxrs.ResultContent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import org.jboss.resteasy.reactive.RestResponse
import java.time.LocalDateTime

@ApplicationScoped
@Path("/api/core")
class DevelopmentController(
    val config: ApplicationConfig
) {
    @GET
    @Path("/ping")
    fun ping(): RestResponse<Content<MutableMap<String, Any?>>> {
        val metadata = mutableMapOf<String, Any?>()
        metadata["name"] = config.name()
        metadata["environment"] = config.environment()
        metadata["dateTime"] = LocalDateTime.now()

        return ResultContent.of(metadata)
            .withMessage("Ping server test successful")
            .build()
    }
}