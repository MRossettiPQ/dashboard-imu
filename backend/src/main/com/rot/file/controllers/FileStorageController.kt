package com.rot.file.controllers

import com.rot.core.jaxrs.ResultContent
import com.rot.file.models.FileStorage
import com.rot.user.enums.UserRoleString
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestPath
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

}