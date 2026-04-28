package com.rot.file.services

import com.rot.core.exceptions.ApplicationException
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.URISyntaxException
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardOpenOption

class LocalStorageService private constructor() {
    lateinit var baseFolder: String

    @Throws(URISyntaxException::class)
    fun init(baseFolder: String) {
        this.baseFolder = baseFolder
    }

    fun removeObject(path: String): Boolean {
        val deleted = getFile(path).delete()
        if (!deleted) throw ApplicationException("File not deleted")
        return true
    }

    fun putObject(path: String, file: File): File {
        return try {
            putObject(path, FileInputStream(file))
        } catch (e: FileNotFoundException) {
            throw ApplicationException("Inválid file to upload in LocalStorage")
        }
    }

    fun putObject(path: String, inputStream: InputStream): File {
        val file = getFile(path)
        file.createNewFile()
        file.outputStream()
            .use {
                inputStream.copyTo(it)
            }
        if(!doesObjectExist(path)) {
            throw ApplicationException("Arquivo não está no LocalStorage")
        }
        return file
    }

    fun getObject(path: String): URL {
        return getFile(path).toURI().toURL()
    }

    fun doesObjectExist(path: String): Boolean {
        return getFile(path).exists()
    }

    @Throws(IOException::class)
    fun download(path: String): File {
        return getFile(path)
    }

    private fun getFile(path: String): File {
        val targetPath = File(baseFolder, path)
        val absolutePath = targetPath.normalize().absolutePath
        val file = File(absolutePath)

        val checkParent = file.parentFile
        if (!checkParent.exists()) {
            checkParent.mkdirs()
        }
        return file
    }

    @Throws(IOException::class)
    fun getInputStream(path: String): InputStream {
        return Files.newInputStream(getFile(path).toPath(), StandardOpenOption.READ)
    }

    companion object {
        private val instances: MutableMap<String, LocalStorageService> = mutableMapOf()

        @Synchronized
        fun getOrInit(
            key: String = "default",
            baseFolder: String? = null
        ): LocalStorageService {
            return instances.getOrPut(key) {
                if (baseFolder == null) throw ApplicationException("Base folder deve ser informado na primeira inicialização de '$key'")
                LocalStorageService().apply {
                    init(baseFolder)
                }
            }
        }

        fun retrieve(key: String = "default"): LocalStorageService {
            return instances[key]
                ?: throw IllegalStateException("Instância '$key' do LocalStorageService não foi inicializada")
        }
    }
}