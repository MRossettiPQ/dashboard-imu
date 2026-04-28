package com.rot.core.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object HashingUtils {
    fun md5(bytes: ByteArray?): String {
        val md: MessageDigest = try {
            MessageDigest.getInstance("MD5")
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalArgumentException("Error on generate MD5 hash", e)
        }
        md.update(bytes)
        val byteData = md.digest()

        //convert the byte to hex format method 1
        val sb = StringBuffer()
        for (i in byteData.indices) {
            sb.append(((byteData[i].toInt() and 0xff) + 0x100).toString(16).substring(1))
        }
        return sb.toString()
    }
}
