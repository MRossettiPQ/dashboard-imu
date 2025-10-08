package com.rot.session.controllers

import com.rot.core.jaxrs.ResultContent
import com.rot.session.dtos.MeasurementDto
import com.rot.session.dtos.MeasurementPaginationResponse
import com.rot.session.models.Measurement
import com.rot.session.models.Movement
import com.rot.session.models.Sensor
import com.rot.session.services.SciLabServices
import io.quarkus.security.Authenticated
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery
import java.util.*

@Authenticated
@ApplicationScoped
@Path("/api/movements")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class MovementController(
    private val sciLabServices: SciLabServices
) {

    @GET
    @Path("/{uuid}/calculate-variability")
    @Operation(
        summary = "Calculate movement variability",
        description = "Calculate the variability center for the specified movement"
    )
    @APIResponse(
        responseCode = "200",
        description = "Movement variability calculated successfully"
    )
    @APIResponse(responseCode = "404", description = "Movement not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    fun calculateVariability(@RestPath("uuid") uuid: UUID): Response? {
        val movement = Movement.findOrThrowById(uuid)
        val result = sciLabServices.calculateVariabilityCenter(movement)
        return ResultContent.of(result).build()
    }

    @GET
    @Path("/{movement}/sensors/{sensor}/measurements")
    @Operation(
        summary = "Paginated list of measurements for a specific movement and sensor",
        description = "Retrieve a paginated list of measurements associated with a given movement and sensor"
    )
    @APIResponse(
        responseCode = "200",
        description = "Paginated list of measurements for the specified movement and sensor",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = Schema(implementation = MeasurementPaginationResponse::class),
            )
        ]
    )
    @APIResponse(responseCode = "500", description = "Internal server error")
    fun listMeasurements(
        @RestPath("sensor") sensor: UUID,
        @RestPath("movement") movement: UUID,
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int,
    ): Response {
        val query = Measurement.createQuery()
            .leftJoin(Sensor.q)
            .on(Measurement.q.sensor().id.eq(Sensor.q.id))
            .leftJoin(Movement.q)
            .on(Sensor.q.movement().id.eq(Movement.q.id))
            .where(Sensor.q.id.eq(sensor))
            .where(Movement.q.id.eq(movement))

        return ResultContent.of(Measurement.fetch(query, page, rpp))
            .transform(MeasurementDto::from)
            .build()
    }

}