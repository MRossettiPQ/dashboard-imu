package com.rot.mqtt.dto

import com.rot.measurement.dtos.MeasurementRead
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(description = "Payload para registro de um novo sensor na rede")
data class SensorRegisterPayload(
    @field:Schema(description = "Endereço MAC do equipamento", example = "00:1B:44:11:3A:B7")
    val mac: String? = null,

    @field:Schema(description = "Nome de identificação do sensor", example = "Sensor de Umidade Estufa 1")
    val name: String? = null,

    @field:Schema(description = "Endereço IP atual do sensor", example = "192.168.0.150")
    val ip: String? = null,
)

@Schema(description = "Payload contendo um lote de medições publicadas pelo sensor")
data class MeasurementBatchPayload(
    @field:Schema(description = "Identificador de origem (gateway ou zona)", example = "gw-zone-north")
    val originIdentifier: String? = null,

    @field:Schema(description = "Lista das medições realizadas")
    val content: MutableList<MeasurementRead>? = null,
)

@Schema(description = "Payload informando o status atual e saúde do sensor")
data class SensorStatusPayload(
    @field:Schema(description = "Status de operação", example = "ONLINE")
    val status: String? = null,

    @field:Schema(description = "Mensagem ou código detalhado do status", example = "Operando normalmente")
    val message: String? = null,
)