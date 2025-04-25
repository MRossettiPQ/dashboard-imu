package com.rot.core.utils

import io.quarkus.logging.Log

object ProcessUtils {

    fun processBuilder(command: List<String>): Process {
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader()
        output.forEachLine { Log.info(it) }

        return process
    }

}
