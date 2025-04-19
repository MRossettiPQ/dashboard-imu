package com.rot.core.jaxrs


import jakarta.ws.rs.core.Response
import java.io.Serializable

class ResultContent<T> : Serializable {

    private var statusCode: Int = 200
    private var data: ContentDto<T> = ContentDto()

    fun withStatusCode(code: Response.Status): ResultContent<T> {
        statusCode = code.statusCode
        data.code = code.statusCode
        return this
    }

    fun withStatusCode(code: Int): ResultContent<T> {
        statusCode = code
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
            .entity(data)
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

class ContentDto<T> : Serializable {
    var code: Int = 200
    var message: String? = null
    var content: T? = null
}

class Pagination<T> : Serializable {
    var page: Int = 1
    var rpp: Int = 10
    var hasMore: Boolean = true
    var list: List<T> = mutableListOf()
    var extra: MutableMap<String, Any?> = mutableMapOf()
}