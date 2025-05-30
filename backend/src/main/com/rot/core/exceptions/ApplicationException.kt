package com.rot.core.exceptions

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.rot.core.jaxrs.ResultContent
import com.rot.core.utils.JsonUtils
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.ws.rs.core.Response
import java.lang.Exception

class ApplicationException(
    override val message: String,
    private val statusCode: Int? = 500,
    private val exception: Exception? = null,
) : RuntimeException(message) {

    private var content: ObjectNode? = null

    constructor(message: String, status: Response.Status, exception: Exception? = null) :
            this(message, status.statusCode, exception)

    constructor(ex: ConstraintViolationException) : this(
        message = "Validation failed for request body.",
        statusCode = Response.Status.BAD_REQUEST.statusCode,
        exception = ex
    ) {
        content = JsonUtils.MAPPER.createObjectNode()
        println(ex.message)
        content!!.put("title", "Constraint Violation")
        val violations: ArrayNode = JsonUtils.MAPPER.createArrayNode()

        ex.constraintViolations.forEach { violation: ConstraintViolation<*> ->
            violations.add(
                JsonUtils.MAPPER.createObjectNode().apply {
                    put("field", violation.propertyPath.toString())
                    put("message", violation.message)
                }
            )
        }

        content!!.set<ArrayNode>("violations", violations)
    }

    fun toResponse(): Response {
        exception?.printStackTrace()
        return ResultContent.of()
            .withContent(content?.let { JsonUtils.MAPPER.convertValue(content, Map::class.java) })
            .withStatusCode(statusCode!!)
            .withMessage(message)
            .build()
    }

}