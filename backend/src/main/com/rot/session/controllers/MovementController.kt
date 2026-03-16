package com.rot.session.controllers

import com.rot.core.jaxrs.Content
import com.rot.core.jaxrs.Pagination
import com.rot.core.jaxrs.ResultContent
import com.rot.measurement.dtos.MeasurementDto
import com.rot.measurement.models.Measurement
import com.rot.session.models.Movement
import com.rot.session.models.SessionSensor
import com.rot.session.services.SciLabServices
import io.quarkus.security.Authenticated
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery
import org.jboss.resteasy.reactive.RestResponse

@Authenticated
@ApplicationScoped
@Path("/api/movements")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class MovementController(
    private val sciLabServices: SciLabServices
) {

//    @GET
//    @Transactional
//    @Path("/{id}/calculate-variability")
//    @Operation(
//        summary = "Calcular variabilidade do movimento",
//        description = "Calcula o centro de variabilidade para o movimento especificado"
//    )
//    @APIResponse(
//        responseCode = "200",
//        description = "Variabilidade do movimento calculada com sucesso"
//    )
//    @APIResponse(responseCode = "401", description = "Autenticação inválida")
//    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
//    @APIResponse(responseCode = "404", description = "Movimento não encontrado")
//    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
//    fun calculateVariability(
//        @RestPath("id") id: Int
//    ): RestResponse<Content<List<Pair<MeasurementDto, MeasurementDto>>>> {
//        val movement = Movement.findOrThrowById(id)
//        val result = sciLabServices.calculateVariabilityCenter(movement)
//        return ResultContent.of(result).build()
//    }

    @GET
    @Transactional
    @Path("/sensors/{sensor}/measurements")
    @Operation(
        summary = "Listagem paginada de medições por movimento e sensor",
        description = "Recupera uma lista paginada de medições associadas a um movimento e sensor específicos"
    )
    @APIResponse(
        responseCode = "200",
        description = "Lista paginada de medições recuperada com sucesso",
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "404", description = "Sensor não encontrado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun listMeasurements(
        @RestPath("sensor") sensor: Int,
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int,
    ): RestResponse<Content<Pagination<MeasurementDto>>> {
        val sessionSensor = SessionSensor.findOrThrowById(sensor)

        val query = Measurement.createQuery()
            .leftJoin(SessionSensor.q)
            .on(Measurement.q.sessionSensor().id.eq(SessionSensor.q.id))
            .where(SessionSensor.q.id.eq(sessionSensor.id))

        return ResultContent.of(Measurement.fetch(query, page, rpp))
            .transform(MeasurementDto::from)
            .build()
    }

}