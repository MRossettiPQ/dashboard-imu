package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.measurement.models.Measurement
import com.rot.measurement.models.SensorInfo
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull


@Entity
@Table(
    name = "session_sensors",
    indexes = [
        Index(name = "idx_session_sensors_session", columnList = "session_id"),
        Index(name = "idx_session_sensors_info", columnList = "sensor_info_id"),
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_session_sensors", columnNames = ["session_id", "sensor_info_id"])
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class SessionSensor : BaseEntity<SessionSensor>() {
    companion object : BaseCompanion<SessionSensor, Int, QSessionSensor> {
        override val entityClass: Class<SessionSensor> = SessionSensor::class.java
        override val q: QSessionSensor = QSessionSensor.sessionSensor
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    var session: Session? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_info_id", nullable = false)
    var sensorInfo: SensorInfo? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sessionSensor", cascade = [CascadeType.ALL])
    var nodeSensors = mutableSetOf<NodeSensor>()

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sessionSensor", cascade = [CascadeType.ALL])
    var measurements = mutableSetOf<Measurement>()
}