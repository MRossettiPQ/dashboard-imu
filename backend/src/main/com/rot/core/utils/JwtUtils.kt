package com.rot.core.utils

import com.rot.core.config.ApplicationConfig
import com.rot.core.exceptions.ApplicationException
import io.quarkus.logging.Log
import io.smallrye.jwt.auth.principal.DefaultJWTParser
import io.smallrye.jwt.auth.principal.ParseException
import io.smallrye.jwt.build.Jwt
import io.smallrye.jwt.util.KeyUtils
import org.eclipse.microprofile.jwt.JsonWebToken
import java.security.PrivateKey
import java.time.Duration
import java.time.Instant

object JwtUtils {

    private val privateKey: PrivateKey by lazy {
        KeyUtils.readPrivateKey("/META-INF/resources/security/private.pem")
    }

    fun generate(
        issuer: String,
        subject: String,
        claims: MutableMap<String, Any?> = mutableMapOf(),
        groups: MutableSet<String> = mutableSetOf(),
        audience: String = "default-audience",
        clientType: String = "internal",
        duration: Duration = Duration.ofHours(1),
        username: String? = null,
    ): String {
        val now = Instant.now()
        val expiry = now.plusSeconds(duration.seconds) // 1 hour validity

        val builder = Jwt.claims()
            .upn(username)
            .issuer(issuer)
            .subject(subject)
            .audience(audience)
            .groups(groups)
            .issuedAt(now.epochSecond)
            .expiresIn(expiry.epochSecond)
            .claim("client_type", clientType)

        claims.forEach { (key, value) ->
            builder.claim(key, value)
        }

        return builder.sign(privateKey)
    }

    fun decode(token: String) = try {
        DefaultJWTParser().parse(token)
    } catch (e: ParseException) {
        Log.error("Inválid token: ${e.message}")
        null
    }

    fun validate(token: String): JsonWebToken {
        return try {
            val decoded = decode(token)
                ?: throw ApplicationException("Token inválido ou expirado", 401)

            val issuer = decoded.issuer
            if (issuer != ApplicationConfig.config.security().issuer()) {
                throw ApplicationException("Invalid issuer", 401)
            }

            decoded
        } catch (e: Exception) {
            throw ApplicationException("Invalid JWT token", 401, e)
        }
    }

}