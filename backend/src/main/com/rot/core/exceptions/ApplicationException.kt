package com.rot.core.exceptions

import com.rot.core.jaxrs.ResultContent
import jakarta.ws.rs.core.Response
import java.lang.Exception

class ApplicationException(
    override val message: String? = null,
    private val statusCode: Int? = 400,
    private val exception: Exception? = null,
) : RuntimeException(message) {

    fun toResponse(): Response {
        exception?.printStackTrace()
        return ResultContent.of()
            .withStatusCode(statusCode!!)
            .withMessage(message)
            .build()
    }

}