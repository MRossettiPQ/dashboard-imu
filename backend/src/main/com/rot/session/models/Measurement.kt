package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.session.enums.*
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*


@Entity
@Table(
    name = "measurements",
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

    @NotEmpty
    @Column(name = "captured_at", nullable = false, updatable = false)
    var capturedAt: LocalDateTime? = null

    @NotEmpty
    @Column(name = "sensor_name", nullable = false, updatable = false)
    var sensorName: String? = null

    @NotNull
    @Column(name = "measurement_order", nullable = false, updatable = false)
    var measurementOrder: Int? = null

//    @NotNull
//    @Column(name = "acc_x", nullable = false, updatable = false)
//    var accX: BigDecimal? = null
//
//    @NotNull
//    @Column(name = "acc_y", nullable = false, updatable = false)
//    var accY: BigDecimal? = null
//
//    @NotNull
//    @Column(name = "acc_z", nullable = false, updatable = false)
//    var accZ: BigDecimal? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "sensor_id", nullable = false)
    var sensor: Sensor? = null

}
