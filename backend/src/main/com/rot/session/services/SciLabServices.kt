package com.rot.session.services

import com.rot.core.exceptions.ApplicationException
import com.rot.session.dtos.MeasurementDto
import com.rot.session.enums.PositionEnum
import com.rot.session.models.Measurement
import com.rot.session.models.Movement
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime


@ApplicationScoped
class SciLabServices {

    fun checkTolerance(time1: LocalDateTime, time2: LocalDateTime, tolerance: Duration): Boolean {
        return Duration.between(time1, time2).abs() <= tolerance
    }

    fun averageMeasurements(measurements: List<Measurement>): MeasurementDto {
        fun averageOf(selector: (Measurement) -> BigDecimal?): BigDecimal? {
            val values = measurements.mapNotNull(selector)
            return if (values.isNotEmpty()) values.reduce { acc, bd -> acc + bd } / BigDecimal(values.size) else null
        }

        return MeasurementDto().apply {
            capturedAt = measurements.firstOrNull()?.capturedAt
            sensorName = measurements.firstOrNull()?.sensorName
            readOrder = measurements.firstOrNull()?.readOrder

            accelMssX = averageOf { it.accelMssX }
            accelMssY = averageOf { it.accelMssY }
            accelMssZ = averageOf { it.accelMssZ }

            accelLinX = averageOf { it.accelLinX }
            accelLinY = averageOf { it.accelLinY }
            accelLinZ = averageOf { it.accelLinZ }

            gyroRadsX = averageOf { it.gyroRadsX }
            gyroRadsY = averageOf { it.gyroRadsY }
            gyroRadsZ = averageOf { it.gyroRadsZ }

            roll = averageOf { it.roll }
            pitch = averageOf { it.pitch }
            yaw = averageOf { it.yaw }

            eulerX = averageOf { it.eulerX }
            eulerY = averageOf { it.eulerY }
            eulerZ = averageOf { it.eulerZ }

            quaternionX = averageOf { it.quaternionX }
            quaternionY = averageOf { it.quaternionY }
            quaternionZ = averageOf { it.quaternionZ }
            quaternionW = averageOf { it.quaternionW }
        }
    }

    fun calculateVariabilityCenter(movement: Movement): List<Pair<MeasurementDto, MeasurementDto>> {
        Log.info("Starting variability center calculation")

        if (movement.sensors.isEmpty()) {
            throw ApplicationException("No sensors detected")
        }

        val sensor1 = movement.sensors.find { it.position == PositionEnum.ONE }
            ?: throw ApplicationException("No detected sensor position one")
        val measurement1 = sensor1.measurements
            .sortedBy { it.readOrder }
            .groupBy { it.capturedAt!! }
        val sensor2 = movement.sensors.find { it.position == PositionEnum.TWO }
            ?: throw ApplicationException("No detected sensor position two")
        val measurement2 = sensor2.measurements
            .sortedBy { it.readOrder }
            .groupBy { it.capturedAt!! }

        Log.info("Sensor ONE: ${sensor1.sensorName}, total measurements: ${sensor1.measurements.size}")
        Log.info("Sensor TWO: ${sensor2.sensorName}, total measurements: ${sensor2.measurements.size}")

        val tolerance = Duration.ofMillis(50)
        val syncList = mutableMapOf<LocalDateTime, Pair<List<Measurement>, List<Measurement>>>()
        for ((time1, group1) in measurement1) {
            // Tenta encontrar algum timestamp em measurement2 dentro da tolerância
            val group2 = measurement2.entries
                .find { (time2, _) -> checkTolerance(time1, time2, tolerance) }

            if (group2 != null) {
                syncList[time1] = Pair(group1, group2.value)
            } else {
                Log.debug("No matching timestamp found for $time1 in sensor TWO within tolerance")
            }
        }

        val length = syncList.size
        Log.info("Total synchronized timestamp pairs: $length")
        val averagedPairs = syncList.map { (_, groups) ->
            val avg1 = averageMeasurements(groups.first)
            val avg2 = averageMeasurements(groups.second)
            avg1 to avg2
        }

        Log.info("Diferença sensor1: ${measurement1.size - syncList.size}")
        Log.info("Diferença sensor2: ${measurement2.size - syncList.size}")
        Log.info("Variability center calculation completed")
        return averagedPairs
    }
}