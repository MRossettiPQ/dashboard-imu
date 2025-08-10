package com.rot.core.jaxrs

import com.querydsl.jpa.JPQLQuery
import com.rot.core.exceptions.ApplicationException
import com.rot.core.hibernate.structures.BaseCompanion
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.time.LocalDateTime
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.companionObjectInstance

class ResultContent<T> {
    private var statusCode: Int = 200
    private var internalCode: Int = 0
    private var data: ContentDto<T> = ContentDto()
    private var type: String = MediaType.APPLICATION_JSON
    private var headers: MutableMap<String, Any> = mutableMapOf()
    private var fields: List<KMutableProperty1<out T, *>> = emptyList()

    private fun filterSelectedFields(): Any {
        val content = data.content ?: return data

        return ContentDto<Any>().apply {
            this.httpCode = data.httpCode
            this.code = data.code
            this.message = data.message
            this.content = content
        }
    }

    fun filterFields(fields: List<KMutableProperty1<out T, *>>): ResultContent<T> {
        this.fields = fields
        return this
    }

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

    fun withHeader(key: String, value: Any): ResultContent<T> {
        headers[key] = value
        return this
    }

    fun <R> transform(fn: (T) -> R): ResultContent<R> {
        val newResult = ResultContent<R>()
        newResult.statusCode = statusCode
        newResult.data.httpCode = data.httpCode
        newResult.data.message = data.message
        newResult.headers = headers
        return ResultContent<R>().withContent(fn(data.content!!))
    }

    fun build(): Response {
        val builder = Response.status(statusCode)
            .type(type)
            .entity(if (fields.isNotEmpty()) filterSelectedFields() else data)

        headers.forEach {
            builder.header(it.key, it.value)
        }

        return builder.build()
    }

    companion object {
        fun of(): ResultContent<Any> {
            return ResultContent()
        }

        fun <T> of(content: T): ResultContent<T> {
            return ResultContent<T>().withContent(content)
        }

        fun <T> of(entityClass: Class<*>, query: JPQLQuery<T>, page: Int? = 1, rpp: Int? = 10, fetchCount: Boolean = true): ResultContent<PaginationDto<T>> {
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
open class ContentDto<T> {
    @Schema(description = "Response timestamp")
    var date: LocalDateTime = LocalDateTime.now()

    @Schema(description = "HTTP status code of the response")
    var httpCode: Int = 200

    @Schema(description = "Internal response code")
    var code: Int = 0

    @Schema(description = "Optional response message")
    var message: String? = null

    @Schema(description = "Response payload")
    var content: T? = null
}

@Schema(description = "Paginated API response")
open class PaginationDto<T> {
    @Schema(description = "Current page")
    var page: Int = 1

    @Schema(description = "Records per page")
    var rpp: Int = 10

    @Schema(description = "Total number of records")
    var count: Long = 0

    @Schema(description = "Indicates whether there are more pages")
    var hasMore: Boolean = true

    @Schema(description = "List of results")
    var list: MutableList<T> = mutableListOf()

    @Schema(description = "Additional information")
    var extra: MutableMap<String, Any?> = mutableMapOf()

    fun <R> transform(fn: (T) -> R): PaginationDto<R> {
        val transformed = PaginationDto<R>()
        transformed.page = page
        transformed.rpp = rpp
        transformed.hasMore = hasMore
        transformed.count = count
        transformed.list = list.map(fn).toMutableList()
        transformed.extra = extra
        return transformed
    }

    fun addExtraData(key: String, value: Any?): PaginationDto<T> {
        this.extra[key] = value
        return this
    }
}
