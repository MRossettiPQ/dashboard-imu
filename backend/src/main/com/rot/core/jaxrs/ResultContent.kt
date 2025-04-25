package com.rot.core.jaxrs


import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

class ResultContent<T> {

    private var statusCode: Int = 200
    private var data: ContentDto<T> = ContentDto()
    private var fields: String? = null
    private var type: String = MediaType.APPLICATION_JSON

    fun withStatusCode(code: Response.Status): ResultContent<T> {
        statusCode = code.statusCode
        data.code = code.statusCode
        return this
    }

    fun filterFields(fields: String): ResultContent<T> {
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

    fun withStatusCode(code: Int): ResultContent<T> {
        statusCode = code
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

    fun build(): Response {
        return Response.status(statusCode)
            .type(type)
            .entity(if (fields != null) null else data)
            .build()
    }

    companion object {
        fun of(): ResultContent<Any> {
            return ResultContent()
        }

        fun <T> of(content: T): ResultContent<T> {
            return ResultContent<T>().withContent(content)
        }
    }
}

class ContentDto<T> {
    var code: Int = 200
    var message: String? = null
    var content: T? = null
}

class Pagination<T> {
    var page: Int = 1
    var rpp: Int = 10
    var count: Long = 0
    var hasMore: Boolean = true
    var list: MutableList<T> = mutableListOf()
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