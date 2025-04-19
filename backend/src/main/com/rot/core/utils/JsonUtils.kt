package com.rot.core.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object JsonUtils {
    val MAPPER = createMapper()

    private fun createMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        val javaTimeModule = JavaTimeModule()

        val localDateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        javaTimeModule.addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(localDateTimePattern))
        mapper.registerModules(javaTimeModule)
        mapper.registerModules(Jdk8Module())
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false) //ISO-8601
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, false) // "" -> null
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) //Ignora atributos nao encontrados no Objeto
        mapper.dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")

        val reflectionModule = KotlinModule.Builder()
            .withReflectionCacheSize(512)
            .build()
        mapper.registerModule(reflectionModule)

        return mapper
    }

    fun toJson(obj: Any?): JsonNode {
        return try {
            MAPPER.valueToTree(obj)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun <T> toObject(node: JsonNode, clazz: Class<T>): T {
        return MAPPER.convertValue(node, clazz)
    }

    fun <T> toObject(string: String, clazz: Class<T>): T {
        return MAPPER.readValue(string, clazz)
    }

    fun <T> toObject(byteArray: ByteArray, clazz: Class<T>): T {
        return MAPPER.readValue(byteArray, clazz)
    }

    fun toJsonString(value: Any?): String {
        return try {
            MAPPER.writeValueAsString(value)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun toJsonNode(value: String?): JsonNode {
        return try {
            MAPPER.readTree(value)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}