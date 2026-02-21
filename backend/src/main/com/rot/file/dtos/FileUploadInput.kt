package com.rot.file.dtos

import com.rot.core.jaxrs.PaginationDto
import com.rot.file.enums.FileStorageEnum
import com.rot.session.enums.MovementEnum
import com.rot.session.models.AngleRule
import com.rot.session.models.MovementType
import jakarta.ws.rs.core.MediaType
import org.jboss.resteasy.reactive.PartType
import org.jboss.resteasy.reactive.RestForm
import org.jboss.resteasy.reactive.multipart.FileUpload

class FileUploadInput {
    @RestForm("file") // Nome do campo no form-data
    // @PartType(MediaType.APPLICATION_OCTET_STREAM) // Opcional, ajuda na documentação OpenAPI
    var file: FileUpload? = null

    @RestForm
    @PartType(MediaType.TEXT_PLAIN)
    var description: String? = null

    @RestForm
    @PartType(MediaType.TEXT_PLAIN)
    var storageMethod: FileStorageEnum? = null
}