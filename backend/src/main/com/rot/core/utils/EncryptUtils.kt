package com.rot.core.utils

import java.io.InputStream
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object EncryptUtils {
    private const val RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
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

    fun encryptWithPublicKey(text: String, publicKey: PublicKey): String {
        val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedBytes = cipher.doFinal(text.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decryptWithPrivateKey(encryptedText: String, privateKey: PrivateKey): String {
        val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText))
        return String(decryptedBytes)
    }

    fun publicKeyFromBase64(base64Key: String): PublicKey {
        val keyBytes = Base64.getDecoder().decode(base64Key)
        val keySpec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance(RSA_TRANSFORMATION).generatePublic(keySpec)
    }

    fun privateKeyFromBase64(base64Key: String): PrivateKey {
        val keyBytes = Base64.getDecoder().decode(base64Key)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance(RSA_TRANSFORMATION).generatePrivate(keySpec)
    }

    fun publicKeyToBase64(publicKey: PublicKey): String {
        return Base64.getEncoder().encodeToString(publicKey.encoded)
    }

    fun privateKeyToBase64(privateKey: PrivateKey): String {
        return Base64.getEncoder().encodeToString(privateKey.encoded)
    }

    // ========== LOAD KEYS FROM FILE ==========
    fun loadPublicKey(path: String): PublicKey {
        val inputStream = ResourceUtils.getJarResourceStream(path)
        val keyBytes = readPemKey(inputStream, "PUBLIC")
        val keySpec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance(RSA_TRANSFORMATION).generatePublic(keySpec)
    }

    fun loadPrivateKey(path: String): PrivateKey {
        val inputStream = ResourceUtils.getJarResourceStream(path)
        val keyBytes = readPemKey(inputStream)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance(RSA_TRANSFORMATION).generatePrivate(keySpec)
    }

    private fun readPemKey(inputStream: InputStream, type: String = "PRIVATE"): ByteArray {
        var content = inputStream.bufferedReader().use { it.readText() }
        content = content
            .replace("-----BEGIN ${type.uppercase()} KEY-----", "")
            .replace("-----END ${type.uppercase()} KEY-----", "")
            .replace("\\s".toRegex(), "")
        return Base64.getDecoder().decode(content)
    }

}
