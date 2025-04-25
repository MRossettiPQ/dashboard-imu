package com.oais.app.controllers

import com.rot.core.config.ApplicationConfig
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertTrue

@QuarkusTest
class DevelopmentControllerTest {

    @Test
    fun getPingShouldReturnApplicationMetadata() {
        given()
            .`when`()
            .get("/api/core/ping")
            .then()
            .statusCode(200)
            .body("content.name", notNullValue())
            .body("content.environment", notNullValue())
            .body("content.dateTime", notNullValue())
    }

    @Test
    fun getPingShouldReturnExpectedMetadata() {
        val beforeCall = LocalDateTime.now()

        val response = given()
            .`when`()
            .get("/api/core/ping")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        val name = response.getString("content.name")
        val environment = response.getString("content.environment")
        val dateTimeString = response.getString("content.dateTime")

        val returnedDateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val afterCall = LocalDateTime.now()

        assertEquals(ApplicationConfig.config.name(), name)
        assertEquals(ApplicationConfig.config.environment(), environment)

        // Verifica se a data está dentro do intervalo de tolerância de 3 segundos
        val minTime = beforeCall.minusSeconds(3)
        val maxTime = afterCall.plusSeconds(3)

        assertTrue(returnedDateTime in minTime..maxTime, "Returned dateTime $returnedDateTime is not within the 3-second tolerance window of now")
    }

}