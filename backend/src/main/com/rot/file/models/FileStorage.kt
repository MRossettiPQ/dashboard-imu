package com.rot.file.models

import com.querydsl.core.annotations.Config
import com.rot.core.exceptions.ApplicationException
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.core.utils.HashingUtils
import com.rot.file.enums.FileStorageEnum
import com.rot.file.enums.ModificationEnum
import io.quarkus.logging.Log
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.eclipse.microprofile.config.ConfigProvider
import org.hibernate.annotations.ColumnDefault
import java.io.*
import java.net.URL
import java.net.URLConnection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


@Entity
@Table(
    name = "files",
    indexes = [
        Index(name = "idx_file_filename", columnList = "file_name"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class FileStorage() : BaseEntity<FileStorage>() {
    companion object : BaseCompanion<FileStorage, UUID, QFileStorage> {
        override val entityClass: Class<FileStorage> = FileStorage::class.java
        override val q: QFileStorage = QFileStorage.fileStorage
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    override var id: UUID? = null

    @Column(name = "description", nullable = false)
    var description: String? = null

    @Column(name = "file_name")
    var fileName: String? = null

    @NotNull
    @Column(name = "file_name_normalized")
    var fileNameNormalized: String? = null

    @Column(name = "content_type")
    var contentType: String? = null

    @Column(name = "extension")
    var extension: String? = null

    @Column(name = "length")
    var length: Long? = null

    @NotNull
    @Column(name = "deleted")
    var deleted: Boolean = false

    @NotNull
    @Column(name = "is_file")
    var isFile: Boolean = true

    @NotNull
    @Column(name = "symbolic_link")
    var symbolicLink: Boolean = false

    @Column(name = "storage_absolute_path")
    var storageAbsolutePath: String? = null

    @Column(name = "storage_relative_path")
    var storageRelativePath: String? = null

    @Column(name = "hash")
    var hash: String? = null

    @NotNull
    @Column(name = "origin", columnDefinition = "varchar(255) default 'unknown'")
    var origin: String = "default"

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "bytes_fallback")
    var bytesFallback: ByteArray? = null

    @NotNull
    @Column(name = "stored_at", nullable = false, updatable = false)
    var storedAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "moved_at")
    var movedAt: LocalDateTime? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_method", nullable = false)
    var storageMethod: FileStorageEnum = FileStorageEnum.LOCAL

    @NotNull
    @ColumnDefault("'ORIGINAL'")
    @Enumerated(EnumType.STRING)
    @Column(name = "volume_status", nullable = false)
    var volumeStatus: ModificationEnum = ModificationEnum.ORIGINAL

    @Transient
    var inputStream: InputStream? = null
        get() = field ?: storageMethod.getInputStream(this)

    @get:Throws(IOException::class)
    val url: URL?
        get() = storageMethod.getUrl(this)

    @Throws(IOException::class)
    fun download(): File {
        return storageMethod.download(this)
    }

    @get:Throws(IOException::class)
    open val content: ByteArray?
        get() = bytesFallback ?: storageMethod.getBytes(this)

    @get:Transient
    val fileUrl: String
        get() = "${ConfigProvider.getConfig().getValue("application.backend.base-url", String::class.java)}/api/files/$id"

    fun removePhysicalFile() {
        storageMethod.remove(this)
    }

    fun checkExist(): Boolean {
        return storageMethod.check(this)
    }

    @get:Transient
    val getBasePath: String
        get() = storageMethod.getBasePath(this)

    constructor(
        file: File,
        fileName: String? = null,
        storageRelativePath: String? = null,
        contentType: String? = null,
        storageMethod: FileStorageEnum? = null,
        symbolicLink: Boolean? = false,
        origin: String? = null,
    ) : this(
        inputStream = FileInputStream(file),
        fileName = fileName ?: file.name,
        storageRelativePath = storageRelativePath,
        contentType = contentType,
        length = file.length(),
        isFile = file.isFile,
        storageMethod = storageMethod,
        symbolicLink = symbolicLink,
        storageAbsolutePath = file.absolutePath,
        origin = origin
    )

    constructor(
        inputStream: InputStream,
        fileName: String,
        contentType: String? = null,
        hash: String? = null,
        length: Long? = null,
        isFile: Boolean? = true,
        storageMethod: FileStorageEnum? = FileStorageEnum.LOCAL,
        symbolicLink: Boolean? = false,
        storageRelativePath: String? = null,
        storageAbsolutePath: String? = null,
        origin: String? = null,
    ) : this() {
        this.isFile = isFile == true
        this.symbolicLink = symbolicLink == true
        this.inputStream = inputStream
        this.fileName = fileName
        this.storageMethod = storageMethod ?: FileStorageEnum.LOCAL
        this.length = length
        this.hash = hash
        this.storageRelativePath = storageRelativePath
        this.origin = origin ?: "default"
        this.contentType = contentType

        if (this.symbolicLink) {
            this.storageAbsolutePath = storageAbsolutePath
        }

        if (isNewBean && this.isFile) {
            try {
                val bytes = IOUtils.toByteArray(inputStream)
                this.length = bytes.size.toLong()
                this.hash = HashingUtils.md5(bytes)
                this.inputStream = ByteArrayInputStream(bytes)
                this.bytesFallback = bytes
                inputStream.close()
            } catch (e: IOException) {
                Log.error(e.message)
                throw e
            }
        }
    }

    fun moveStorage(targetStorage: FileStorageEnum, targetOrigin: String, removeOriginAfterMove: Boolean = true): FileStorage {
        if (symbolicLink || !this.isFile) {
            throw ApplicationException("Não suportado para links simbólicos ou pastas")
        }

        if (isNewBean) {
            throw ApplicationException("Não suportado para novos arquivos")
        }

        if (storageMethod == targetStorage && origin == targetOrigin) {
            throw ApplicationException("Não pode mover para o mesmo local de origem")
        }

        return storageMethod.move(this, targetStorage, targetOrigin, removeOriginAfterMove)
    }

    override fun save(): FileStorage {
        if (isNewBean && isFile) {
            try {
                contentType = contentType ?: URLConnection.guessContentTypeFromStream(inputStream)
                extension = FilenameUtils.getExtension(fileName)

                var absolutePath = storageRelativePath
                var relativePath = storageRelativePath

                if (!symbolicLink) {
                    fileNameNormalized = buildString {
                        append(System.currentTimeMillis())
                        append('-')
                        append(UUID.randomUUID())
                        extension?.let { append(".$it") }
                    }

                    val basePath = storageMethod.getBasePath(this)
                    val date = storedAt.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val hour = storedAt.format(DateTimeFormatter.ofPattern("HH"))

                    val pathParts = listOfNotNull(relativePath, date, hour, fileNameNormalized)
                    relativePath = pathParts.joinToString("/")
                    absolutePath = (listOf(basePath) + pathParts).joinToString("/")
                }

                storageRelativePath = relativePath?.replace("//", "/")
                storageAbsolutePath = absolutePath?.replace("//", "/")

                if (!symbolicLink) {
                    storageMethod.persist(this)
                }
            } catch (e: IOException) {
                Log.error(e.message, e)
                removePhysicalFile()
            } finally {
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    Log.error(e.message)
                }
            }
        }
        return super.save()
    }

}
