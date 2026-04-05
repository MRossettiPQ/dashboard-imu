package com.rot.session.controllers

import com.querydsl.core.types.dsl.Expressions
import com.rot.core.jaxrs.Content
import com.rot.core.jaxrs.Pagination
import com.rot.core.jaxrs.ResultContent
import com.rot.measurement.dtos.SensorInfoRead
import com.rot.measurement.models.SensorInfo
import com.rot.mqtt.services.MqttBrokerService
import com.rot.session.dtos.*
import com.rot.session.services.SessionService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.jboss.resteasy.reactive.RestQuery
import org.jboss.resteasy.reactive.RestResponse
import java.util.*

@ApplicationScoped
@Path("/api/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class SessionController(
    private val sessionService: SessionService,
    private val mqttBrokerService: MqttBrokerService,
) {

    @POST
    @Transactional
    @Path("/create")
    @Operation(
        summary = "Registrar uma nova sessão de medição",
        description = "Cria uma nova sessão de medição e retorna os seus dados"
    )
    @APIResponse(
        responseCode = "200",
        description = "Sessão registrada com sucesso",
    )
    @APIResponse(responseCode = "401", description = "Autenticação inválida")
    @APIResponse(responseCode = "403", description = "Acesso negado ou usuário não autenticado")
    @APIResponse(responseCode = "500", description = "Erro interno do servidor")
    fun create(body: SessionCreateOrUpdate): RestResponse<Content<SessionRead>> {
        val session = sessionService.create(body)
        return ResultContent.of(session)
            .withStatusCode(Response.Status.OK)
            .transform(SessionRead::from)
            .build()
    }

    @POST
    @Path("/{sessionId}/sensors/{macAddress}")
    @Operation(summary = "Adicionar sensor à sessão")
    fun addSensor(
        @PathParam("sessionId") sessionId: UUID,
        @PathParam("macAddress") macAddress: String
    ): RestResponse<Content<SessionSensorRead>> {
        val sessionSensor = sessionService.addSensor(sessionId, macAddress)
        return ResultContent.of(sessionSensor)
            .transform(SessionSensorRead::from)
            .build()
    }

    @DELETE
    @Path("/{sessionId}/sensors/{macAddress}")
    @Operation(summary = "Remover sensor da sessão")
    fun removeSensor(
        @PathParam("sessionId") sessionId: UUID,
        @PathParam("macAddress") macAddress: String
    ): RestResponse<Content<Any>> {
        sessionService.removeSensor(sessionId, macAddress)
        return ResultContent.of()
            .withMessage("Sensor removido")
            .build()
    }

    @POST
    @Path("/{sessionId}/start")
    @Operation(summary = "Iniciar coleta de medições")
    fun start(@PathParam("sessionId") sessionId: UUID): RestResponse<Content<Any>> {
        sessionService.start(sessionId)
        return ResultContent.of()
            .withMessage("Sessão iniciada")
            .build()
    }

    @POST
    @Path("/{sessionId}/stop")
    @Operation(summary = "Parar coleta de medições")
    fun stop(@PathParam("sessionId") sessionId: UUID): RestResponse<Content<Any>> {
        sessionService.stop(sessionId)
        return ResultContent.of()
            .withMessage("Sessão parada")
            .build()
    }

    @POST
    @Path("/{sessionId}/finalize")
    @Operation(summary = "Finalizar sessão completamente")
    fun finalize(@PathParam("sessionId") sessionId: UUID): RestResponse<Content<Any>> {
        sessionService.finalize(sessionId)
        return ResultContent.of()
            .withMessage("Sessão finalizada")
            .build()
    }

    @POST
    @Path("/sensors/{macAddress}/calibrate")
    @Operation(summary = "Solicitar calibração de um sensor")
    fun calibrateSensor(@PathParam("macAddress") macAddress: String): RestResponse<Content<Any>> {
        sessionService.calibrateSensor(macAddress)
        return ResultContent.of()
            .withMessage("Calibração solicitada")
            .build()
    }

    @GET
    @Path("/sensors/available")
    @Transactional
    @Operation(summary = "Listar sensores conectados e disponíveis")
    fun listAvailableSensors(
        @DefaultValue("1") @RestQuery page: Int,
        @DefaultValue("10") @RestQuery rpp: Int,
    ): RestResponse<Content<Pagination<SensorInfoRead>>> {
        val availableMacs = mqttBrokerService.connectedSensors
            .filter { (_, ctx) -> ctx.available }
            .keys
            .toList()

        var query = SensorInfo.createQuery()
            .where(SensorInfo.q.active.isTrue)
            .where(SensorInfo.q.macAddress.`in`(availableMacs))

        if (availableMacs.isEmpty()) {
            query = query.where(Expressions.asBoolean(false).isTrue)
        }

        return ResultContent.of(SensorInfo.fetch(query, page, rpp))
            .transform(SensorInfoRead::from)
            .build()
    }

    @GET
    @Path("/sensors/connected")
    @Operation(summary = "Listar todos os sensores conectados ao broker")
    fun listConnectedSensors(): RestResponse<Content<List<SensorSessionContext>>> {
        val sensors = mqttBrokerService.connectedSensors.map { (_, ctx) -> ctx }
        return ResultContent.of(sensors).build()
    }

    @GET
    @Path("/active")
    @Operation(summary = "Listar sessões ativas em memória")
    fun listActiveSessions(): RestResponse<Content<List<UserSessionContext>>> {
        val sessions = mqttBrokerService.activeSessions.map { (_, ctx) -> ctx }
        return ResultContent.of(sessions).build()
    }
}