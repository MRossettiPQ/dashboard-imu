package com.rot.core.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.rot.core.config.ApplicationConfig
import com.rot.core.exceptions.ApplicationException
import io.quarkus.logging.Log
import java.time.Instant
import java.util.*

object JwtUtils {

    fun decode(token: String) = try {
        JWT.decode(token)
    } catch (e: JWTDecodeException) {
        Log.error("Inválid token: ${e.message}")
        null
    }

    fun generate(
        issuer: String,
        subject: String,
        claims: MutableMap<String, Any> = mutableMapOf(),
        audience: String = "default-audience",
        clientType: String = "internal",
    ): String {
        val algorithm = Algorithm.HMAC256(ApplicationConfig.config.security().token())
        val now = Date()
        val expiry = Date(now.time + 3600_000)

        val builder = JWT.create()
            .withIssuer(issuer)
            .withSubject(subject)
            .withAudience(audience)
            .withIssuedAt(now)
            .withExpiresAt(expiry)
            .withClaim("client_type", clientType)

        claims.forEach { (key, value) ->
            when (value) {
                is String -> builder.withClaim(key, value)
                is Int -> builder.withClaim(key, value)
                is Long -> builder.withClaim(key, value)
                is Boolean -> builder.withClaim(key, value)
                is Double -> builder.withClaim(key, value)
                is Date -> builder.withClaim(key, value)
                is Instant -> builder.withClaim(key, value)
                is List<*> -> builder.withClaim(key, value)
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    builder.withClaim(key, value as Map<String, Any>)
                }
                else -> builder.withClaim(key, value.toString())
            }
        }

        return builder.sign(algorithm)
    }

    fun decode(issuer: String, token: String): DecodedJWT {
        return try {
            val algorithm = Algorithm.HMAC256(ApplicationConfig.config.security().token())
            val verificador = JWT.require(algorithm)
                .withIssuer(issuer)
                .build()
            verificador.verify(token)
        } catch (ex: JWTVerificationException) {
            throw ApplicationException("Invalid JWT token", 400, ex)
        }
    }

}