package com.rot.mqtt.dto

import com.rot.measurement.dtos.MeasurementRead

data class SensorRegisterPayload(
    val mac: String? = null,
    val name: String? = null,
    val ip: String? = null,
)

data class MeasurementBatchPayload(
    val originIdentifier: String? = null,
    val content: MutableList<MeasurementRead>? = null,
)

data class SensorStatusPayload(
    val status: String? = null,
    val message: String? = null,
)