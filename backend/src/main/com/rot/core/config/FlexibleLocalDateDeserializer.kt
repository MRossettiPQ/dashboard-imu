package com.rot.core.config

import io.quarkus.jsonb.JsonbConfigCustomizer
import jakarta.inject.Singleton
import jakarta.json.bind.JsonbConfig
import jakarta.json.bind.serializer.DeserializationContext
import jakarta.json.bind.serializer.JsonbDeserializer
import jakarta.json.stream.JsonParser
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

class FlexibleLocalDateDeserializer : JsonbDeserializer<LocalDate> {
    override fun deserialize(parser: JsonParser, ctx: DeserializationContext, rtType: Type): LocalDate {
        val value = parser.string
        return try {
            LocalDate.parse(value)
        } catch (e: DateTimeParseException) {
            OffsetDateTime.parse(value).toLocalDate()
        }
    }
}

@Singleton
class JsonbCustomizerConfig : JsonbConfigCustomizer {
    override fun customize(config: JsonbConfig) {
        config.withDeserializers(FlexibleLocalDateDeserializer())
    }
}
