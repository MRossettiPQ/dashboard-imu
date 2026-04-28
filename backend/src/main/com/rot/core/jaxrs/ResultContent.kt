package com.rot.core.jaxrs

import com.querydsl.jpa.JPQLQuery
import com.rot.core.exceptions.ApplicationException
import com.rot.core.hibernate.structures.BaseCompanion
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.jboss.resteasy.reactive.RestResponse
import java.time.LocalDateTime
import kotlin.reflect.full.companionObjectInstance

class ResultContent<T : Any?> {
    private var statusCode: Int = 200
    private var internalCode: Int = 0
    private var data: Content<T> = Content()
    private var type: String = MediaType.APPLICATION_JSON
    private var headers: MutableMap<String, Any?> = mutableMapOf()

    fun withType(type: MediaType): ResultContent<T> {
        this.type = type.type
        return this
    }

    fun withType(type: String): ResultContent<T> {
        this.type = type
        return this
    }

    fun withStatusCode(code: Response.Status): ResultContent<T> {
        statusCode = code.statusCode
        data.httpCode = code.statusCode
        return this
    }

    fun withStatusCode(code: Int): ResultContent<T> {
        statusCode = code
        data.httpCode = code
        return this
    }

    fun withInternalCode(code: Int): ResultContent<T> {
        internalCode = code
        data.code = code
        return this
    }

    fun withContent(content: T? = null): ResultContent<T> {
        data.content = content
        return this
    }

    fun withMessage(message: String? = null): ResultContent<T> {
        data.message = message
        return this
    }

    fun withHeader(key: String, value: Any?): ResultContent<T> {
        headers[key] = value
        return this
    }

    fun <R : Any> transform(fn: (T) -> R): ResultContent<R> {
        val newResult = ResultContent<R>()
        newResult.statusCode = statusCode
        newResult.data.httpCode = data.httpCode
        newResult.data.message = data.message
        newResult.headers = headers
        return of(fn(data.content!!))
    }

    fun buildResponse(): Response {
        val builder = Response.status(statusCode)
            .type(type)
            .entity(data)

        headers.forEach {
            builder.header(it.key, it.value)
        }

        return builder.build()
    }

    fun build(): RestResponse<Content<T>> {
        val status = RestResponse.Status.fromStatusCode(statusCode)
        val builder = RestResponse.ResponseBuilder.create(status, data)

        builder.type(type)

        headers.forEach {
            builder.header(it.key, it.value)
        }

        return builder.build()
    }

    companion object {
        fun of(): ResultContent<Any> {
            return ResultContent()
        }

        fun <T : Any?> of(content: T): ResultContent<T> {
            return ResultContent<T>().withContent(content)
        }

        fun <T> of(entityClass: Class<*>, query: JPQLQuery<T>, page: Int? = 1, rpp: Int? = 10, fetchCount: Boolean = true): ResultContent<Pagination<T>> {
            val companion = entityClass.kotlin.companionObjectInstance
                ?: throw ApplicationException("Entity ${entityClass.simpleName} não possui companion object")

            require(companion is BaseCompanion<*, *, *>) {
                "Companion de ${entityClass.simpleName} não implementa BaseCompanion"
            }

            return of(companion.fetch(query, page, rpp, fetchCount))
        }
    }
}

@Schema(description = "Generic API response")
open class Content<T : Any?> {
    @Schema(description = "Response timestamp", required = true)
    var date: LocalDateTime = LocalDateTime.now()

    @Schema(description = "HTTP status code of the response", required = true)
    var httpCode: Int = 200

    @Schema(description = "Internal response code", required = true)
    var code: Int = 0

    @Schema(description = "Optional response message", type = SchemaType.STRING)
    var message: String? = null

    @Schema(
        description = "Response payload",
        required = true,
        nullable = false
    )
    var content: T? = null
}

@Schema(description = "Paginated API response")
open class Pagination<T> {
    @Schema(description = "Current page", required = true)
    var page: Int = 1

    @Schema(description = "Records per page", required = true)
    var rpp: Int = 10

    @Schema(description = "Total number of records", required = true)
    var count: Long = 0

    @Schema(description = "Indicates whether there are more pages", required = true)
    var hasMore: Boolean = true

    @Schema(description = "List of results", required = true)
    var list: MutableList<T> = mutableListOf()

    @Schema(description = "Additional information", required = true)
    var extra: MutableMap<String, Any?> = mutableMapOf()

    fun <R> transform(fn: (T) -> R): Pagination<R> {
        val transformed = Pagination<R>()
        transformed.page = page
        transformed.rpp = rpp
        transformed.hasMore = hasMore
        transformed.count = count
        transformed.list = list.map(fn).toMutableList()
        transformed.extra = extra
        return transformed
    }

    fun addExtraData(key: String, value: Any?): Pagination<T> {
        this.extra[key] = value
        return this
    }
}
