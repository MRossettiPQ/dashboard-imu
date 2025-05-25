package com.rot.core.providers

import com.rot.core.exceptions.ApplicationException
import io.quarkus.qute.Location
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import jakarta.ws.rs.ClientErrorException
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider


@Provider
class ApplicationExceptionProvider : ExceptionMapper<ApplicationException> {
    override fun toResponse(exception: ApplicationException) = exception.toResponse()
}

@Provider
class ExceptionProvider : ExceptionMapper<Exception> {
    override fun toResponse(e: Exception): Response {
        e.printStackTrace()
        return ApplicationException("Undefined error", 500).toResponse()
    }
}

@Provider
class RuntimeExceptionProvider : ExceptionMapper<ClientErrorException> {
    override fun toResponse(e: ClientErrorException): Response {
        return ApplicationException("An error occurred - ${e.message} - ${e.cause?.message}", e.response.status, e)
            .toResponse()
    }
}

@Provider
class NotFoundExceptionMapper(
    @Location("404") val template: Template,
) : ExceptionMapper<NotFoundException> {

    @Context
    lateinit var uriInfo: UriInfo

    override fun toResponse(exception: NotFoundException): Response {
        val path = uriInfo.path
        val renderedTemplate: TemplateInstance = template.data("path", "/$path")

        return Response.status(Response.Status.NOT_FOUND)
            .entity(renderedTemplate)
            .build()
    }
}