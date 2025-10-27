package com.rot.core.utils

import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.collections.indices
import kotlin.text.substring

object HashingUtils {
    fun md5(bytes: ByteArray?): String {
        val md: MessageDigest
        md = try {
            MessageDigest.getInstance("MD5")
        } catch (e: NoSuchAlgorithmException) {
            throw kotlin.IllegalArgumentException("Error on generate MD5 hash", e)
        }
        md.update(bytes)
        val byteData = md.digest()

        //convert the byte to hex format method 1
        val sb = StringBuffer()
        for (i in byteData.indices) {
            sb.append(Integer.toString((byteData[i].toInt() and 0xff) + 0x100, 16).substring(1))
        }
        return sb.toString()
    }
}
