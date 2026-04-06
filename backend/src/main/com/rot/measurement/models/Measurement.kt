package com.rot.measurement.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.session.models.SessionSensor
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "measurements",
    indexes = [
        Index(name = "idx_measurements_session_sensor", columnList = "session_sensor_id"),
        Index(name = "idx_measurements_read_order", columnList = "session_sensor_id, read_order"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class Measurement : BaseEntity<Measurement>() {
    companion object : BaseCompanion<Measurement, UUID, QMeasurement> {
        override val entityClass: Class<Measurement> = Measurement::class.java
        override val q: QMeasurement = QMeasurement.measurement
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
    @Column(name = "accel_x", nullable = false, updatable = false)
    var accelX: Double? = null

    @NotNull
    @Column(name = "accel_y", nullable = false, updatable = false)
    var accelY: Double? = null

    @NotNull
    @Column(name = "accel_z", nullable = false, updatable = false)
    var accelZ: Double? = null

    // --- Accelerometer (m/s²) ---
    @NotNull
    @Column(name = "accel_mss_x", nullable = false, updatable = false)
    var accelMssX: Double? = null

    @NotNull
    @Column(name = "accel_mss_y", nullable = false, updatable = false)
    var accelMssY: Double? = null

    @NotNull
    @Column(name = "accel_mss_z", nullable = false, updatable = false)
    var accelMssZ: Double? = null

    // --- Linear Acceleration ---
    @NotNull
    @Column(name = "accel_lin_x", nullable = false, updatable = false)
    var accelLinX: Double? = null

    @NotNull
    @Column(name = "accel_lin_y", nullable = false, updatable = false)
    var accelLinY: Double? = null

    @NotNull
    @Column(name = "accel_lin_z", nullable = false, updatable = false)
    var accelLinZ: Double? = null

    // --- Gyroscope (rad/s) ---
    @NotNull
    @Column(name = "gyro_rads_x", nullable = false, updatable = false)
    var gyroRadsX: Double? = null

    @NotNull
    @Column(name = "gyro_rads_y", nullable = false, updatable = false)
    var gyroRadsY: Double? = null

    @NotNull
    @Column(name = "gyro_rads_z", nullable = false, updatable = false)
    var gyroRadsZ: Double? = null

    // --- Magnetometer bias ---
    @NotNull
    @Column(name = "mag_x", nullable = false, updatable = false)
    var magX: Double? = null

    @NotNull
    @Column(name = "mag_y", nullable = false, updatable = false)
    var magY: Double? = null

    @NotNull
    @Column(name = "mag_z", nullable = false, updatable = false)
    var magZ: Double? = null

    // --- Euler angles (roll, pitch, yaw) ---
    @NotNull
    @Column(name = "roll", nullable = false, updatable = false)
    var roll: Double? = null

    @NotNull
    @Column(name = "pitch", nullable = false, updatable = false)
    var pitch: Double? = null

    @NotNull
    @Column(name = "yaw", nullable = false, updatable = false)
    var yaw: Double? = null

    // --- Euler (alternative representation) ---
    @NotNull
    @Column(name = "euler_x", nullable = false, updatable = false)
    var eulerX: Double? = null

    @NotNull
    @Column(name = "euler_y", nullable = false, updatable = false)
    var eulerY: Double? = null

    @NotNull
    @Column(name = "euler_z", nullable = false, updatable = false)
    var eulerZ: Double? = null

    // --- Quaternion (for 3D orientation) ---
    @NotNull
    @Column(name = "quaternion_x", nullable = false, updatable = false)
    var quaternionX: Double? = null

    @NotNull
    @Column(name = "quaternion_y", nullable = false, updatable = false)
    var quaternionY: Double? = null

    @NotNull
    @Column(name = "quaternion_z", nullable = false, updatable = false)
    var quaternionZ: Double? = null

    @NotNull
    @Column(name = "quaternion_w", nullable = false, updatable = false)
    var quaternionW: Double? = null

    // --- Relationship ---
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_sensor_id", nullable = false)
    var sessionSensor: SessionSensor? = null
}