package com.rot.measurement.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.session.models.SessionSensor
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "measurements",
    indexes = [
        Index(name = "idx_measurement_session_sensor", columnList = "session_sensor_id"),
        Index(name = "idx_measurement_read_order", columnList = "session_sensor_id, read_order"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class Measurement : BaseEntity<Measurement>() {
    companion object : BaseCompanion<Measurement, UUID, QMeasurement> {
        override val entityClass: Class<Measurement> = Measurement::class.java
        override val q: QMeasurement = QMeasurement.measurement

        fun findAllBySessionSensorId(sessionSensorId: Int): MutableList<Measurement> {
            return createQuery()
                .where(q.sessionSensor().id.eq(sessionSensorId))
                .orderBy(q.readOrder.asc())
                .fetch()
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    override var id: UUID? = null

    @NotNull
    @Column(name = "captured_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    var capturedAt: OffsetDateTime? = null

    @Column(name = "sensor_name", updatable = false)
    var sensorName: String? = null

    @NotNull
    @Column(name = "read_order", nullable = false, updatable = false)
    var readOrder: Int? = null

    // --- Accelerometer (m/s²) ---
    @NotNull
    @Column(name = "accel_mss_x", nullable = false, updatable = false)
    var accelMssX: BigDecimal? = null

    @NotNull
    @Column(name = "accel_mss_y", nullable = false, updatable = false)
    var accelMssY: BigDecimal? = null

    @NotNull
    @Column(name = "accel_mss_z", nullable = false, updatable = false)
    var accelMssZ: BigDecimal? = null

    // --- Linear Acceleration ---
    @NotNull
    @Column(name = "accel_lin_x", nullable = false, updatable = false)
    var accelLinX: BigDecimal? = null

    @NotNull
    @Column(name = "accel_lin_y", nullable = false, updatable = false)
    var accelLinY: BigDecimal? = null

    @NotNull
    @Column(name = "accel_lin_z", nullable = false, updatable = false)
    var accelLinZ: BigDecimal? = null

    // --- Gyroscope (rad/s) ---
    @NotNull
    @Column(name = "gyro_rads_x", nullable = false, updatable = false)
    var gyroRadsX: BigDecimal? = null

    @NotNull
    @Column(name = "gyro_rads_y", nullable = false, updatable = false)
    var gyroRadsY: BigDecimal? = null

    @NotNull
    @Column(name = "gyro_rads_z", nullable = false, updatable = false)
    var gyroRadsZ: BigDecimal? = null

    // --- Magnetometer bias ---
    @NotNull
    @Column(name = "mag_bias_x", nullable = false, updatable = false)
    var magBiasX: BigDecimal? = null

    @NotNull
    @Column(name = "mag_bias_y", nullable = false, updatable = false)
    var magBiasY: BigDecimal? = null

    @NotNull
    @Column(name = "mag_bias_z", nullable = false, updatable = false)
    var magBiasZ: BigDecimal? = null

    // --- Euler angles (roll, pitch, yaw) ---
    @NotNull
    @Column(name = "roll", nullable = false, updatable = false)
    var roll: BigDecimal? = null

    @NotNull
    @Column(name = "pitch", nullable = false, updatable = false)
    var pitch: BigDecimal? = null

    @NotNull
    @Column(name = "yaw", nullable = false, updatable = false)
    var yaw: BigDecimal? = null

    // --- Euler (alternative representation) ---
    @NotNull
    @Column(name = "euler_x", nullable = false, updatable = false)
    var eulerX: BigDecimal? = null

    @NotNull
    @Column(name = "euler_y", nullable = false, updatable = false)
    var eulerY: BigDecimal? = null

    @NotNull
    @Column(name = "euler_z", nullable = false, updatable = false)
    var eulerZ: BigDecimal? = null

    // --- Quaternion (for 3D orientation) ---
    @NotNull
    @Column(name = "quaternion_x", nullable = false, updatable = false)
    var quaternionX: BigDecimal? = null

    @NotNull
    @Column(name = "quaternion_y", nullable = false, updatable = false)
    var quaternionY: BigDecimal? = null

    @NotNull
    @Column(name = "quaternion_z", nullable = false, updatable = false)
    var quaternionZ: BigDecimal? = null

    @NotNull
    @Column(name = "quaternion_w", nullable = false, updatable = false)
    var quaternionW: BigDecimal? = null

    // --- Relationship ---
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_sensor_id", nullable = false)
    var sessionSensor: SessionSensor? = null
}