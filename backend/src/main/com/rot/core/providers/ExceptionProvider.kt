package com.rot.core.providers

import com.rot.core.exceptions.ApplicationException
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider


@Provider
class ApplicationExceptionProvider : ExceptionMapper<ApplicationException> {
    override fun toResponse(exception: ApplicationException) = exception.toResponse()
}