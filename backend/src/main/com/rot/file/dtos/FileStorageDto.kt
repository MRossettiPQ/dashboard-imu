import com.rot.core.jaxrs.PaginationDto
import com.rot.file.enums.FileStorageEnum
import com.rot.file.enums.ModificationEnum
import com.rot.file.models.FileStorage
import com.rot.session.enums.MovementEnum
import java.time.LocalDateTime
import java.util.*


class FileStorageDto {
    var id: UUID? = null
    var type: MovementEnum? = null
    var fileName: String? = null
    var description: String? = null
    var contentType: String? = null
    var extension: String? = null
    var origin: String? = null
    var length: Long? = null
    var storedAt: LocalDateTime? = null
    var storageMethod: FileStorageEnum = FileStorageEnum.LOCAL
    var volumeStatus: ModificationEnum = ModificationEnum.ORIGINAL

    companion object {
        fun from(entity: FileStorage) : FileStorageDto {
            val dto = FileStorageDto()
            dto.id = entity.id
            dto.fileName = entity.fileName
            dto.description = entity.description
            dto.contentType = entity.contentType
            dto.extension = entity.extension
            dto.length = entity.length
            dto.origin = entity.origin
            dto.storedAt = entity.storedAt
            dto.storageMethod = entity.storageMethod
            dto.volumeStatus = entity.volumeStatus
            return dto
        }
        fun from(paginationDto: PaginationDto<FileStorage>): PaginationDto<FileStorageDto> {
            return paginationDto.transform { from(it) }
        }
    }
}