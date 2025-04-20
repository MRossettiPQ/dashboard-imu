package com.rot.core.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.rot.core.exceptions.ApplicationException
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ResourceUtils {
    fun getJarResource(path: String): File {
        val resource = object {}.javaClass.getResource(path)
        val file = File(resource!!.toURI())
        if (!file.exists()) {
            throw ApplicationException("$path file not exists")
        }
        return file
    }
}
