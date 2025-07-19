package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.session.enums.*
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.*


@Entity
@Table(
    name = "sensors",
    indexes = [
        Index(name = "idx_sensor_type", columnList = "type"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class Sensor : BaseEntity<Sensor>() {
    companion object : BaseCompanion<Sensor, UUID, QSensor> {
        override val entityClass: Class<Sensor> = Sensor::class.java
        override val q: QSensor = QSensor.sensor
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    override var id: UUID? = null

    @NotNull
    @NotEmpty
    @Column(name = "mac_address")
    var macAddress: String? = null

    @NotNull
    @NotEmpty
    @Column(name = "ip")
    var ip: String? = null

    @NotNull
    @NotEmpty
    @Column(name = "sensor_name")
    var sensorName: String? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false)
    var position: PositionEnum? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: SensorType = SensorType.GYROSCOPE

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "observation", columnDefinition = "TEXT")
    var observation: String? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "movement_id", nullable = false)
    var movement: Movement? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sensor", cascade = [CascadeType.ALL])
    var measurements = mutableSetOf<Measurement>()
}
