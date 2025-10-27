package com.rot.file.enums

import com.rot.core.exceptions.ApplicationException
import com.rot.file.models.FileStorage
import com.rot.file.services.LocalStorageService
import io.quarkus.logging.Log
import jakarta.ws.rs.core.Response
import org.apache.commons.io.IOUtils
import java.io.*
import java.net.URL
import java.time.LocalDateTime

enum class FileStorageEnum {
    LOCAL {
        fun getService(fileStorage: FileStorage): LocalStorageService {
            return LocalStorageService.retrieve(fileStorage.origin)
        }

        override fun doGetUrl(fileStorage: FileStorage): URL {
            return getService(fileStorage)
                .getObject(fileStorage.storageRelativePath!!)
        }

        @Throws(IOException::class)
        override fun doDownload(fileStorage: FileStorage): File {
            return getService(fileStorage)
                .download(fileStorage.storageRelativePath!!)
        }

        @Throws(IOException::class)
        override fun doGetInputStream(fileStorage: FileStorage): InputStream {
            return getService(fileStorage)
                .getInputStream(fileStorage.storageRelativePath!!)
        }

        @Throws(IOException::class)
        override fun doGetBytes(fileStorage: FileStorage): ByteArray? {
            return try {
                val baos = ByteArrayOutputStream()
                var inputStream = fileStorage.inputStream ?: getService(fileStorage)
                    .getInputStream(fileStorage.storageRelativePath!!)

                if (inputStream.available() == 0) {
                    inputStream = ByteArrayInputStream(fileStorage.bytesFallback)
                }

                IOUtils.copy(inputStream, baos)
                baos.toByteArray()
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        override fun doPersist(fileStorage: FileStorage) {
            getService(fileStorage)
                .putObject(fileStorage.storageRelativePath!!, fileStorage.inputStream!!)
            fileStorage.bytesFallback = null
        }

        override fun doRemove(fileStorage: FileStorage): Boolean {
            return getService(fileStorage)
                .removeObject(fileStorage.storageRelativePath!!)
        }

        override fun doCheck(fileStorage: FileStorage): Boolean {
            return getService(fileStorage)
                .doesObjectExist(fileStorage.storageRelativePath!!)
        }

        override fun doGetBasePath(fileStorage: FileStorage): String {
            return getService(fileStorage).baseFolder
        }
    };

    @Throws(IOException::class)
    protected abstract fun doGetUrl(fileStorage: FileStorage): URL

    @Throws(IOException::class)
    abstract fun doDownload(fileStorage: FileStorage): File

    @Throws(IOException::class)
    protected abstract fun doGetInputStream(fileStorage: FileStorage): InputStream

    @Throws(IOException::class)
    protected abstract fun doGetBytes(fileStorage: FileStorage): ByteArray?

    @Throws(IOException::class)
    protected abstract fun doPersist(fileStorage: FileStorage)

    @Throws(IOException::class)
    protected abstract fun doRemove(fileStorage: FileStorage): Boolean

    @Throws(IOException::class)
    protected abstract fun doGetBasePath(fileStorage: FileStorage): String

    @Throws(IOException::class)
    protected abstract fun doCheck(fileStorage: FileStorage): Boolean

    @Throws(IOException::class)
    fun getUrl(fileStorage: FileStorage): URL? {
        return doGetUrl(fileStorage)
    }

    @Throws(IOException::class)
    fun download(fileStorage: FileStorage): File {
        return if (!fileStorage.deleted) {
            doDownload(fileStorage)
        } else if (fileStorage.bytesFallback != null) {
            val tempFile = File.createTempFile("app", "storage_file." + fileStorage.extension)
            IOUtils.write(fileStorage.bytesFallback, FileOutputStream(tempFile))
            tempFile
        } else {
            throw ApplicationException("Arquivo ${fileStorage.fileName} foi removido")
        }
    }

    fun remove(fileStorage: FileStorage): Boolean {
        return try {
            doRemove(fileStorage)
        } catch (e: IOException) {
            throw ApplicationException("Storage ${fileStorage.fileName} fail", Response.Status.INTERNAL_SERVER_ERROR, e)
        }
    }

    fun persist(fileStorage: FileStorage) {
        try {
            doPersist(fileStorage)
        } catch (e: IOException) {
            throw ApplicationException("Storage ${fileStorage.fileName} fail", Response.Status.INTERNAL_SERVER_ERROR, e)
        }
    }

    @Throws(IOException::class)
    fun getInputStream(fileStorage: FileStorage): InputStream {
        return if(!fileStorage.deleted) {
            doGetInputStream(fileStorage)
        } else if (fileStorage.bytesFallback != null) {
            ByteArrayInputStream(fileStorage.bytesFallback)
        } else {
            throw ApplicationException("Arquivo ${fileStorage.fileName} foi removido")
        }
    }

    @Throws(IOException::class)
    fun getBytes(fileStorage: FileStorage): ByteArray? {
        return if(!fileStorage.deleted) {
            doGetBytes(fileStorage)
        } else if (fileStorage.bytesFallback != null) {
            fileStorage.bytesFallback
        } else {
            throw ApplicationException("Arquivo ${fileStorage.fileName} foi removido")
        }
    }

    fun move(fileStorage: FileStorage, targetStorage: FileStorageEnum, targetOrigin: String, removeOriginAfterMove: Boolean = true): FileStorage {
        return try {
            doMove(fileStorage, targetStorage, targetOrigin, removeOriginAfterMove)
        } catch (e: IOException) {
            throw ApplicationException("Storage ${fileStorage.fileName} fail", Response.Status.INTERNAL_SERVER_ERROR, e)
        }
    }

    @Throws(IOException::class)
    fun doMove(fileStorage: FileStorage, targetStorage: FileStorageEnum, targetOrigin: String, removeOriginAfterMove: Boolean = true): FileStorage {
        try {
            val clone = FileStorage()
            clone.id = fileStorage.id
            clone.storageMethod = fileStorage.storageMethod
            clone.fileName = fileStorage.fileName
            clone.storageRelativePath = fileStorage.storageRelativePath
            clone.storageAbsolutePath = fileStorage.storageAbsolutePath

            // 1. Obter inputStream da origem
            val inputStream = doGetInputStream(fileStorage)

            // 2. Preparar os novos paths baseado no tipo de destino
            val basePath = fileStorage.getBasePath
            val newRelativePath = listOfNotNull(fileStorage.storageRelativePath).joinToString("/")
            val newAbsolutePath = listOfNotNull(basePath, fileStorage.storageRelativePath).joinToString("/")

            // 3. Criar objeto temporário para o destino (sem alterar o original ainda)
            val tempFileStorage = fileStorage.apply {
                this.inputStream = inputStream
                this.origin = targetOrigin
                this.storageMethod = targetStorage
                this.storageRelativePath = newRelativePath
                this.storageAbsolutePath = newAbsolutePath
            }

            // 4. Persistir no destino
            targetStorage.persist(tempFileStorage)

            // 5. Verificar se foi persistido com sucesso
            val exists = targetStorage.check(tempFileStorage)
            if (!exists) {
                throw ApplicationException("Falha na verificação de existência no destino para: $newRelativePath")
            }

            // 6. Atualizar o objeto original com os novos dados
            fileStorage.movedAt = LocalDateTime.now()
            fileStorage.volumeStatus = ModificationEnum.MOVED
            fileStorage.bytesFallback = null

            val result = fileStorage.save()
            if (removeOriginAfterMove) clone.removePhysicalFile()
            Log.info("Arquivo movido com sucesso de $this para $targetStorage")
            return result
        } catch (e: Exception) {
            Log.error("Erro ao mover arquivo: ${e.message}", e)
            throw ApplicationException("Falha na movimentação do arquivo ${fileStorage.fileName}: ${e.message}", Response.Status.INTERNAL_SERVER_ERROR, e)
        }
    }

    fun check(fileStorage: FileStorage): Boolean {
        return try {
            doCheck(fileStorage)
        } catch (e: IOException) {
            throw ApplicationException("Storage ${fileStorage.fileName} fail", 500, e)
        }
    }

    fun getBasePath(fileStorage: FileStorage): String {
        return try {
            doGetBasePath(fileStorage)
        } catch (e: IOException) {
            throw ApplicationException("Storage ${fileStorage.fileName} fail", 500, e)
        }
    }
}