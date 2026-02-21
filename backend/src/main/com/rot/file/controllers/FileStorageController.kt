package com.rot.file.controllers

import com.rot.core.exceptions.ApplicationException
import com.rot.core.jaxrs.ResultContent
import com.rot.file.dtos.FileUploadInput
import com.rot.file.enums.FileStorageEnum
import com.rot.file.models.FileStorage
import com.rot.user.enums.UserRoleString
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.BeanParam
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.MultipartForm
import org.jboss.resteasy.reactive.RestPath
import java.nio.file.Files
import java.util.*

@ApplicationScoped
@Path("/api/files")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(UserRoleString.PHYSIOTHERAPIST, UserRoleString.ADMINISTRATOR)
class FileStorageController {

    @GET
    @Path("/{uuid}")
    @Transactional
    fun download(@RestPath("uuid") uuid: UUID): Response {
        val fileStorage = FileStorage.findOrThrowById(uuid)
        return ResultContent.of(fileStorage.content)
            .withHeader("Content-Disposition", "inline;filename=${fileStorage.fileName}")
            .withHeader("Content-Type", fileStorage.contentType)
            .build()
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    fun upload(@BeanParam input: FileUploadInput): Response {
        if (input.file == null) {
            throw ApplicationException("O arquivo é obrigatório.")
        }

        val uploadedFile = input.file!!
        val inputStream = Files.newInputStream(uploadedFile.uploadedFile())
        val fileStorage = inputStream.use {
            val fileStorage = FileStorage(
                inputStream = inputStream,
                fileName = uploadedFile.fileName(),
                contentType = uploadedFile.contentType(),
                storageMethod = input.storageMethod ?: FileStorageEnum.DATABASE,
                length = uploadedFile.size()
            )
            fileStorage.description = input.description
            fileStorage.save()
        }

        return Response.status(Response.Status.CREATED)
            .entity(fileStorage)
            .build()
    }

}