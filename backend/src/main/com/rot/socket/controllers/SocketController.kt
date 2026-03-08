package com.rot.socket.controllers

import com.rot.core.jaxrs.Content
import com.rot.core.jaxrs.Pagination
import com.rot.core.jaxrs.ResultContent
import com.rot.measurement.dtos.MeasurementDto
import com.rot.measurement.dtos.MeasurementDto.Companion.from
import com.rot.measurement.dtos.SensorDto
import com.rot.measurement.dtos.SensorInfoDto
import com.rot.measurement.models.Measurement
import com.rot.measurement.models.Sensor
import com.rot.measurement.models.SensorInfo
import com.rot.session.models.Movement
import com.rot.socket.services.SocketService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery
import org.jboss.resteasy.reactive.RestResponse

@ApplicationScoped
@Path("/api/sessions")
class SocketController(
    val socketService: SocketService
) {
    @GET
    @Path("/")
    fun list(): RestResponse<Content<MutableMap<String, Any?>>> {
        val metadata = mutableMapOf<String, Any?>()
        metadata["sessions"] = socketService.sessions

        return ResultContent.of(metadata)
            .build()
    }

    @GET
    @Transactional
    @Path("/sensors/available")
    @Operation(
        summary = "Listagem paginada de sensores disponíveis",
        description = "Recupera uma lista paginada de sensores disponíveis no sistema"
    )
    @APIResponse(
        responseCode = "200",
        description = "Lista paginada de sensores recuperada com sucesso",
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun listAvailableSensors(
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int,
    ): RestResponse<Content<Pagination<SensorInfoDto>>> {
        val availableSensors = socketService.sensors.mapNotNull { it.mac }
        val query = SensorInfo.createQuery()
            .where(SensorInfo.q.active.isTrue)
            .where(SensorInfo.q.macAddress.`in`(availableSensors))

        return ResultContent.of(SensorInfo.fetch(query, page, rpp))
            .transform(SensorInfoDto::from)
            .build()
    }
}