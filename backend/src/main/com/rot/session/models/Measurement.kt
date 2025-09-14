package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*


@Entity
@Table(
    name = "measurements",
    indexes = [
        Index(name = "idx_measurement_sensor", columnList = "sensor_id"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class Measurement : BaseEntity<Measurement>() {
    companion object : BaseCompanion<Measurement, UUID, QMeasurement> {
        override val entityClass: Class<Measurement> = Measurement::class.java
        override val q: QMeasurement = QMeasurement.measurement

        fun findAllBySensorId(sensorId: UUID): MutableList<Measurement> {
            return createQuery()
                .where(q.sensor().id.eq(sensorId))
                .fetch()
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    override var id: UUID? = null

    @NotEmpty
    @Column(name = "captured_at", nullable = false, updatable = false)
    var capturedAt: LocalDateTime? = null

    @NotEmpty
    @Column(name = "sensor_name", nullable = false, updatable = false)
    var sensorName: String? = null

    @NotNull
    @Column(name = "read_order", nullable = false, updatable = false)
    var readOrder: Int? = null

    @NotNull
    @Column(name = "accel_mss_x", nullable = false, updatable = false)
    var accelMssX: BigDecimal? = null

    @NotNull
    @Column(name = "accel_mss_y", nullable = false, updatable = false)
    var accelMssY: BigDecimal? = null

    @NotNull
    @Column(name = "accel_mss_z", nullable = false, updatable = false)
    var accelMssZ: BigDecimal? = null

    @NotNull
    @Column(name = "accel_lin_x", nullable = false, updatable = false)
    var accelLinX: BigDecimal? = null

    @NotNull
    @Column(name = "accel_lin_y", nullable = false, updatable = false)
    var accelLinY: BigDecimal? = null

    @NotNull
    @Column(name = "accel_lin_z", nullable = false, updatable = false)
    var accelLinZ: BigDecimal? = null

    @NotNull
    @Column(name = "gyro_rads_x", nullable = false, updatable = false)
    var gyroRadsX: BigDecimal? = null

    @NotNull
    @Column(name = "gyro_rads_y", nullable = false, updatable = false)
    var gyroRadsY: BigDecimal? = null

    @NotNull
    @Column(name = "gyro_rads_z", nullable = false, updatable = false)
    var gyroRadsZ: BigDecimal? = null

    @NotNull
    @Column(name = "roll", nullable = false, updatable = false)
    var roll: BigDecimal? = null

    @NotNull
    @Column(name = "pitch", nullable = false, updatable = false)
    var pitch: BigDecimal? = null

    @NotNull
    @Column(name = "yaw", nullable = false, updatable = false)
    var yaw: BigDecimal? = null

    @NotNull
    @Column(name = "euler_x", nullable = false, updatable = false)
    var eulerX: BigDecimal? = null

    @NotNull
    @Column(name = "euler_y", nullable = false, updatable = false)
    var eulerY: BigDecimal? = null

    @NotNull
    @Column(name = "euler_z", nullable = false, updatable = false)
    var eulerZ: BigDecimal? = null

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

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "sensor_id", nullable = false)
    var sensor: Sensor? = null
}
