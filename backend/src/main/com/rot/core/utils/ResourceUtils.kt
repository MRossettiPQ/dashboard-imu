package com.rot.core.utils

import com.rot.core.exceptions.ApplicationException
import java.io.File
import java.io.InputStream

object ResourceUtils {

    fun getJarResourceStream(path: String): InputStream {
        return getJarResource(path).inputStream()
    }

    fun getJarResource(path: String): File {
        val resource = javaClass.getResource(path)
        val file = File(resource!!.toURI())
        if (!file.exists()) {
            throw ApplicationException("$path file not exists")
        }
        return file
    }
}
