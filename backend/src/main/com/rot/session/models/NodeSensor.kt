package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull


@Entity
@Table(
    name = "node_sensors",
    indexes = [
        Index(name = "idx_node_sensor_node", columnList = "session_node_id"),
        Index(name = "idx_node_sensor_sensor", columnList = "session_sensor_id"),
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_node_sensor", columnNames = ["session_node_id", "session_sensor_id"])
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class NodeSensor : BaseEntity<NodeSensor>() {
    companion object : BaseCompanion<NodeSensor, Int, QNodeSensor> {
        override val entityClass: Class<NodeSensor> = NodeSensor::class.java
        override val q: QNodeSensor = QNodeSensor.nodeSensor
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Int? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_node_id", nullable = false)
    var sessionNode: SessionNode? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_sensor_id", nullable = false)
    var sessionSensor: SessionSensor? = null
}