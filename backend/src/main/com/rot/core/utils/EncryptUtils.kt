package com.rot.core.utils

import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object EncryptUtils {
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256

    private fun generateByteArraySalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(32)
        random.nextBytes(salt)
        return salt
    }

    fun generateSalt(): String {
        val salt = generateByteArraySalt()
        return Base64.getEncoder().encodeToString(salt)
    }

    fun generateHash(password: String, salt: String): String {
        val combinedSalt = salt.toByteArray()
        val factory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        val spec = PBEKeySpec(password.toCharArray(), combinedSalt, ITERATIONS, KEY_LENGTH)
        val key = factory.generateSecret(spec)
        val hash = key.encoded
        return Base64.getEncoder().encodeToString(hash)
    }

}
