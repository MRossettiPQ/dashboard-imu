package com.rot.core.jaxrs


import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.time.LocalDateTime
import kotlin.reflect.KMutableProperty1

class ResultContent<T> {
    private var statusCode: Int = 200
    private var data: ContentDto<T> = ContentDto()
    private var type: String = MediaType.APPLICATION_JSON
    private var headers: MutableMap<String, Any> = mutableMapOf()
    private var fields: List<KMutableProperty1<out T, *>> = emptyList()

    private fun filterSelectedFields(): Any {
        val content = data.content ?: return data

        return ContentDto<Any>().apply {
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
        data.code = code.statusCode
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

    fun withHeader(key: String, value: Any): ResultContent<T> {
        headers[key] = value
        return this
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
    }
}

@Schema(description = "Resposta genérica da API")
open class ContentDto<T> {
    @Schema(description = "Horário da resposta")
    private var date: LocalDateTime = LocalDateTime.now()

    @Schema(description = "Código HTTP da resposta")
    var code: Int = 200

    @Schema(description = "Mensagem da resposta (opcional)")
    var message: String? = null

    @Schema(description = "Conteúdo da resposta")
    var content: T? = null
}

@Schema(description = "Resposta paginada da API")
class Pagination<T> {
    @Schema(description = "Página atual")
    var page: Int = 1
    @Schema(description = "Registros por página")
    var rpp: Int = 10
    @Schema(description = "Total de registros")
    var count: Long = 0
    @Schema(description = "Se há mais páginas")
    var hasMore: Boolean = true
    @Schema(description = "Lista de resultados")
    var list: MutableList<T> = mutableListOf()
    @Schema(description = "Informações adicionais")
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