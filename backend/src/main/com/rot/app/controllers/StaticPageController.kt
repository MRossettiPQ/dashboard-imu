package com.rot.app.controllers

import io.quarkus.qute.Location
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType


@Path("/")
@ApplicationScoped
class StaticPageResource(
    @Location("index-fallback") val template: Template,
) {

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun showFallback(): TemplateInstance {
        return template.instance()
    }

//    @GET
//    fun redirectToIndex(): Response {
//        return Response.ok()
//            .header("Content-Type", "text/html")
//            .entity(this::class.java.getResourceAsStream("/META-INF/resources/dist/index.html"))
//            .build()
//    }
//
//    private val mimeTypes = mapOf(
//        "html" to MediaType.TEXT_HTML,
//        "js" to "application/javascript",
//        "css" to "text/css",
//        "png" to "image/png",
//        "jpg" to "image/jpeg",
//        "jpeg" to "image/jpeg",
//        "svg" to "image/svg+xml",
//        "json" to MediaType.APPLICATION_JSON,
//        "woff" to "font/woff",
//        "woff2" to "font/woff2",
//        "ttf" to "font/ttf"
//    )
//
//    @GET
//    @Path("{path:.*}")
//    fun index(@PathParam("path") path: String? = null): Response {
//        val normalizedPath = if (path.isNullOrBlank()) "index.html" else path
//        val resourcePath = "/META-INF/resources/spa/$normalizedPath"
//
//        val file = try {
//            val resource = object {}.javaClass.getResource(resourcePath)
//            File(resource!!.toURI())
//        } catch (e: Exception) {
//            throw ApplicationException("File does not exist", 404)
//        }
//
//        // Determine MIME type from file extension
//        val fileExtension = Paths.get(resourcePath).fileName.toString()
//            .substringAfterLast('.', "").lowercase()
//        val mediaType = mimeTypes[fileExtension] ?: MediaType.APPLICATION_OCTET_STREAM
//
//        return Response.ok(file.readBytes()).type(mediaType).build()
//    }
//
}