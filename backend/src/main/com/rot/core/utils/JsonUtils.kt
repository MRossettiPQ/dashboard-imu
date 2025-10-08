package com.rot.core.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object JsonUtils {
    // Instância do ObjectMapper configurado
    val MAPPER = createMapper()

    /**
     * Cria e configura o ObjectMapper com módulos e configurações para tratamento de JSON
     */
    private fun createMapper(): ObjectMapper {
        val mapper = ObjectMapper()

        // Configuração para tratar LocalDateTime com formato específico
        val javaTimeModule = JavaTimeModule()
        val localDateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        javaTimeModule.addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(localDateTimePattern))

        // Registro dos módulos necessários
        mapper.registerModules(javaTimeModule, Jdk8Module())

        // Configurações do mapper
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)  // Usa o formato ISO-8601
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, false)  // "" -> null
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)  // Ignora propriedades desconhecidas

        // Formato de data padrão
        mapper.dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")

        // Configuração para suporte a Kotlin
        val reflectionModule = KotlinModule.Builder()
            .withReflectionCacheSize(512)
            .build()
        mapper.registerModule(reflectionModule)

        return mapper
    }

    fun isValidJson(json: String): Boolean {
        return try {
            MAPPER.readTree(json)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Converte um objeto em JsonNode.
     * @param obj Objeto a ser convertido.
     * @return JsonNode representando o objeto.
     */
    fun toJson(obj: Any?): JsonNode {
        return try {
            MAPPER.valueToTree(obj)
        } catch (e: Exception) {
            throw RuntimeException("Erro ao converter objeto para JsonNode", e)
        }
    }

    /**
     * Converte um JsonNode em um objeto do tipo especificado.
     * @param node JsonNode a ser convertido.
     * @param clazz Classe do tipo para conversão.
     * @return Objeto convertido.
     */
    fun <T> toObject(node: JsonNode, clazz: Class<T>): T {
        return try {
            MAPPER.convertValue(node, clazz)
        } catch (e: Exception) {
            throw RuntimeException("Erro ao converter JsonNode para objeto", e)
        }
    }

    /**
     * Converte uma String JSON em um objeto do tipo especificado.
     * @param string String JSON.
     * @param clazz Classe do tipo para conversão.
     * @return Objeto convertido.
     */
    fun <T> toObject(string: String, clazz: Class<T>): T {
        return try {
            MAPPER.readValue(string, clazz)
        } catch (e: IOException) {
            throw RuntimeException("Erro ao converter String para objeto", e)
        }
    }

    /**
     * Converte um array de bytes JSON em um objeto do tipo especificado.
     * @param data String JSON.
     * @return Objeto convertido.
     */
    inline fun <reified T> toObject(data: String): T {
        return MAPPER.readValue(data, object : TypeReference<T>() {})
    }

    /**
     * Converte um array de bytes JSON em um objeto do tipo especificado.
     * @param byteArray Array de bytes JSON.
     * @return Objeto convertido.
     */
    inline fun <reified T> toObject(byteArray: ByteArray): T {
        return try {
            MAPPER.readValue(byteArray, object : TypeReference<T>() {})
        } catch (e: IOException) {
            throw RuntimeException("Erro ao converter String para objeto", e)
        }
    }

    /**
     * Converte um array de bytes JSON em um objeto do tipo especificado.
     * @param byteArray Array de bytes JSON.
     * @param clazz Classe do tipo para conversão.
     * @return Objeto convertido.
     */
    fun <T> toObject(byteArray: ByteArray, clazz: Class<T>): T {
        return try {
            MAPPER.readValue(byteArray, clazz)
        } catch (e: IOException) {
            throw RuntimeException("Erro ao converter ByteArray para objeto", e)
        }
    }

    /**
     * Converte um objeto em uma String JSON.
     * @param value Objeto a ser convertido.
     * @return String JSON.
     */
    fun toJsonString(value: Any?): String {
        return try {
            MAPPER.writeValueAsString(value)
        } catch (e: IOException) {
            throw RuntimeException("Erro ao converter objeto para String JSON", e)
        }
    }

    /**
     * Converte uma String JSON em JsonNode.
     * @param value String JSON a ser convertida.
     * @return JsonNode.
     */
    fun toJsonNode(value: String?): JsonNode {
        return try {
            MAPPER.readTree(value)
        } catch (e: IOException) {
            throw RuntimeException("Erro ao converter String JSON para JsonNode", e)
        }
    }

    private fun parseFields(fields: String): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        var index = 0

        while (index < fields.length) {
            when (val c = fields[index]) {
                '(' -> {
                    val key = extractKey(fields, index)
                    val (subFields, newIndex) = extractSubFields(fields, index)
                    result[key] = parseFields(subFields)
                    index = newIndex
                }

                ',' -> index++ // skip commas

                else -> {
                    val (field, newIndex) = extractSimpleField(fields, index)
                    if (field.isNotBlank()) result[field] = true
                    index = newIndex
                }
            }
        }

        return result
    }

    private fun extractKey(fields: String, currentIndex: Int): String {
        var i = currentIndex - 1
        val buffer = StringBuilder()
        while (i >= 0 && fields[i] != ',' && fields[i] != ')') {
            buffer.insert(0, fields[i])
            i--
        }
        return buffer.toString().trim()
    }

    private fun extractSubFields(fields: String, startIndex: Int): Pair<String, Int> {
        val stack = ArrayDeque<Char>()
        val sub = StringBuilder()
        var i = startIndex

        stack.addLast('(')
        i++ // skip initial '('

        while (stack.isNotEmpty() && i < fields.length) {
            val ch = fields[i]
            when (ch) {
                '(' -> stack.addLast('(')
                ')' -> stack.removeLast()
            }
            if (stack.isNotEmpty()) sub.append(ch)
            i++
        }

        return sub.toString() to i
    }

    private fun extractSimpleField(fields: String, startIndex: Int): Pair<String, Int> {
        val buffer = StringBuilder()
        var i = startIndex

        while (i < fields.length && fields[i] != ',' && fields[i] != '(' && fields[i] != ')') {
            buffer.append(fields[i])
            i++
        }

        return buffer.toString().trim() to i
    }

    private fun filterFieldsRecursive(node: JsonNode, fields: Map<String, Any>): JsonNode {
        val filtered = MAPPER.createObjectNode()
        println(node)
        for ((field, sub) in fields) {
            println("$field: ${node.has(field)}")
            if (node.has(field)) {
                val value = node.get(field)
                when (sub) {
                    is Boolean -> filtered.set<JsonNode>(field, value)
                    is Map<*, *> -> {
                        if (value.isObject) {
                            @Suppress("UNCHECKED_CAST")
                            filtered.set<JsonNode>(field, filterFieldsRecursive(value, sub as Map<String, Any>))
                        } else {
                            filtered.set(field, value)
                        }
                    }
                }
            }
        }
        println(filtered)
        return filtered
    }

    /**
     * Filtra os campos de um JsonNode com base na lista de campos fornecida.
     * @param entity O JsonNode a ser filtrado.
     * @param fields A lista de campos que devem ser mantidos.
     * @return Um novo JsonNode com apenas os campos especificados.
     */
    fun filterFields(entity: Any?, fields: String?, vararg fieldsToIgnore: String): JsonNode {
        val jsonNode = toJson(entity)
        val fieldList = parseFields(fields ?: return jsonNode)

        val filter = SimpleBeanPropertyFilter.serializeAllExcept(*fieldsToIgnore)
        val filters = SimpleFilterProvider().addFilter("sessionDtoFilter", filter)
        MAPPER.writer(filters).writeValueAsString(jsonNode)

        return filterFieldsRecursive(jsonNode, fieldList)
    }
}
