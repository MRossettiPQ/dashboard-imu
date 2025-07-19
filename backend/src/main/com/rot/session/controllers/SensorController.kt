package com.rot.session.controllers

import com.rot.core.jaxrs.ResultContent
import com.rot.session.dtos.RetrieveMeasurementDto
import com.rot.session.dtos.RetrieveSensorDto
import com.rot.session.models.Measurement
import com.rot.session.models.Sensor
import io.quarkus.security.Authenticated
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery
import java.util.*

@Authenticated
@ApplicationScoped
@Path("/api/sensors")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class SensorController {

    @GET
    @Path("/")
    fun list(
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int,
    ): Response {
        val query = Sensor.createQuery()

        return ResultContent.of()
            .withContent(Sensor.fetch(query, page, rpp).transform(RetrieveSensorDto::from))
            .build()
    }

    @GET
    @Path("/{sensor}/measurements")
    fun measurements(
        @RestPath("sensor") sensor: UUID,
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int,
    ): Response {
        val query = Measurement.createQuery()
            .where(Measurement.q.sensor().id.eq(sensor))

        return ResultContent.of()
            .withContent(Measurement.fetch(query, page, rpp).transform(RetrieveMeasurementDto::from))
            .build()
    }
}