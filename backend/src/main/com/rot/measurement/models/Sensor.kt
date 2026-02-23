package com.rot.measurement.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.session.enums.BodySegmentEnum
import com.rot.session.models.Movement
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

@Entity
@Table(
    name = "sensors",
    indexes = [
        Index(name = "idx_sensor_movement", columnList = "movement_id"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class Sensor : BaseEntity<Sensor>() {
    companion object : BaseCompanion<Sensor, Int, QSensor> {
        override val entityClass: Class<Sensor> = Sensor::class.java
        override val q: QSensor = QSensor.sensor
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Int? = null

    @NotNull
    @NotEmpty
    @Column(name = "ip")
    var ip: String? = null

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "observation", columnDefinition = "TEXT")
    var observation: String? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "body_segment", nullable = false)
    var bodySegment: BodySegmentEnum? = null

    /**
     * Define se é o sensor proximal (mais próximo do tronco) ou distal
     */
    @NotNull
    @Column(name = "is_proximal", nullable = false)
    var isProximal: Boolean = false

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "movement_id", nullable = false)
    var movement: Movement? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "sensor_info_id", nullable = false)
    var sensorInfo: SensorInfo? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sensor", cascade = [CascadeType.ALL])
    var measurements = mutableSetOf<Measurement>()
}