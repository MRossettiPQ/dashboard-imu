package com.rot.app.controllers

import com.rot.core.exceptions.ApplicationException
import com.rot.core.utils.ResourceUtils
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.io.File
import java.nio.file.Paths


@Path("/")
class StaticPageResource {

    private val mimeTypes = mapOf(
        "html" to MediaType.TEXT_HTML,
        "js" to "application/javascript",
        "css" to "text/css",
        "png" to "image/png",
        "jpg" to "image/jpeg",
        "jpeg" to "image/jpeg",
        "svg" to "image/svg+xml",
        "json" to MediaType.APPLICATION_JSON,
        "woff" to "font/woff",
        "woff2" to "font/woff2",
        "ttf" to "font/ttf"
    )

    @GET
    @Path("{path:.*}")
    fun index(@PathParam("path") path: String? = null): Response {
        val normalizedPath = if (path.isNullOrBlank()) "index.html" else path
        val resourcePath = "/META-INF/resources/spa/$normalizedPath"

        val file = try {
            val resource = object {}.javaClass.getResource(resourcePath)
            File(resource!!.toURI())
        } catch (e: Exception) {
            throw ApplicationException("File does not exist", 404)
        }

        // Determine MIME type from file extension
        val fileExtension = Paths.get(resourcePath).fileName.toString()
            .substringAfterLast('.', "").lowercase()
        val mediaType = mimeTypes[fileExtension] ?: MediaType.APPLICATION_OCTET_STREAM

        return Response.ok(file.readBytes()).type(mediaType).build()
    }
}