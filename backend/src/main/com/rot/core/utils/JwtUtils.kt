package com.rot.core.utils

import io.quarkus.logging.Log
import io.smallrye.jwt.auth.principal.DefaultJWTParser
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo
import io.smallrye.jwt.auth.principal.ParseException
import io.smallrye.jwt.build.Jwt
import io.smallrye.jwt.util.KeyUtils
import org.eclipse.microprofile.jwt.JsonWebToken
import java.security.PrivateKey
import java.security.PublicKey
import java.time.Duration
import java.time.Instant

enum class JwtType(val description: String) {
    REFRESH("Refresh"),
    ACCESS("Access")
}

object JwtUtils {

    private val privateKey: PrivateKey by lazy {
        KeyUtils.readPrivateKey("/security/private.pem")
    }

    private val publicKey: PublicKey by lazy {
        KeyUtils.readPublicKey("/security/public.pem")
    }

    fun generate(
        issuer: String,
        subject: String,
        type: JwtType = JwtType.ACCESS,
        claims: MutableMap<String, Any?> = mutableMapOf(),
        groups: MutableSet<String> = mutableSetOf(),
        clientType: String = "internal",
        duration: Duration = Duration.ofHours(1),
        username: String? = null,
    ): String {
        val now = Instant.now()
        val expiry = now.plus(duration) // 1 hour validity

        val builder = Jwt.claims()
            .upn(username)
            .issuer(issuer)
            .subject(subject)
            .audience(issuer)
            .groups(groups)
            .issuedAt(now)
            .expiresAt(expiry)
            .claim("client_type", clientType)
            .claim("token_type", type.name.lowercase())

        claims.forEach { (key, value) ->
            builder.claim(key, value)
        }

        return builder.sign(privateKey)
    }

    fun decode(token: String) = try {
        val parser = DefaultJWTParser()
        parser.parseOnly(token)
    } catch (e: ParseException) {
        Log.error("Inválid token: ${e.message}")
        null
    }

    fun claim(token: String, issuer: String, audience: String): JsonWebToken {
        val context = JWTAuthContextInfo()
        context.clockSkew = 30
        context.publicVerificationKey = publicKey
        context.issuedBy = issuer
        context.expectedAudience = setOf(audience)
        val parser = DefaultJWTParser(context)
        return parser.verify(token, publicKey)
    }

}