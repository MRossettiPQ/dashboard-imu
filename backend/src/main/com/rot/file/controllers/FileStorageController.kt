package com.rot.file.controllers

import FileStorageDto
import com.rot.core.exceptions.ApplicationException
import com.rot.core.jaxrs.Content
import com.rot.core.jaxrs.ResultContent
import com.rot.file.dtos.FileDownloadResponse
import com.rot.file.dtos.FileUploadInput
import com.rot.file.enums.FileStorageEnum
import com.rot.file.models.FileStorage
import com.rot.user.enums.UserRoleString
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content as ContentB
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestResponse
import java.nio.file.Files
import java.util.*


@ApplicationScoped
@Path("/api/files")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(UserRoleString.PHYSIOTHERAPIST, UserRoleString.ADMINISTRATOR)
class FileStorageController {

    @GET
    @Transactional
    @Path("/{uuid}")
    @Operation(
        summary = "Download de arquivo",
        description = "Recupera o conteúdo binário de um arquivo através do seu UUID"
    )
    @APIResponse(
        responseCode = "200",
        description = "Arquivo recuperado com sucesso",
        content = [ContentB(schema = Schema(implementation = FileDownloadResponse::class))]
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "404", description = "Arquivo não encontrado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun download(@RestPath("uuid") uuid: UUID): RestResponse<Content<ByteArray?>> {
        val fileStorage = FileStorage.findOrThrowById(uuid)
        return ResultContent.of(fileStorage.content)
            .withHeader("Content-Disposition", "inline;filename=${fileStorage.fileName}")
            .withHeader("Content-Type", fileStorage.contentType)
            .build()
    }

    @POST
    @Transactional
    @Path("/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Upload de arquivo",
        description = "Realiza o envio de um novo arquivo para o servidor"
    )
    @APIResponse(
        responseCode = "200",
        description = "Arquivo enviado com sucesso",
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun upload(@BeanParam input: FileUploadInput): RestResponse<Content<FileStorageDto>> {
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

        return ResultContent.of(fileStorage)
            .withStatusCode(Response.Status.OK)
            .transform(FileStorageDto::from)
            .withMessage("Arquivo enviado com sucesso")
            .build()
    }

}