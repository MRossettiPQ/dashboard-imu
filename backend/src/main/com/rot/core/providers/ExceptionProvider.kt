package com.rot.core.providers

import com.rot.core.exceptions.ApplicationException
import jakarta.ws.rs.ClientErrorException
import jakarta.ws.rs.core.Response
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