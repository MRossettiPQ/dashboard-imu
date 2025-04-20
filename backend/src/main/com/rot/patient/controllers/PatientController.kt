package com.rot.patient.controllers

import com.rot.core.jaxrs.ResultContent
import com.rot.patient.dtos.PatientDto
import com.rot.patient.models.Patient
import io.quarkus.security.Authenticated
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery
import java.util.*

@Authenticated
@ApplicationScoped
@Path("/api/patients")
class PatientController {

    @GET
    @Path("/{uuid}")
    fun get(@RestPath("uuid") uuid: UUID): Response? {
        val patient = Patient.findOrThrowById(uuid)
        return ResultContent.of().withContent(PatientDto.from(patient)).build()
    }

    @GET
    @Path("/")
    fun list(
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int,
    ): Response? {
        val metadata = mutableMapOf<String, Any>()
        return ResultContent.of().withContent(metadata).build()
    }

}