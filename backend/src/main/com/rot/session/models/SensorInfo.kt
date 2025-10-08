package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.exceptions.ApplicationException
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.session.enums.*
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.core.Response
import java.util.*


@Entity
@Table(
    name = "sensors",
    indexes = [
        Index(name = "idx_sensor_type", columnList = "type"),
        Index(name = "idx_sensor_position", columnList = "position"),
        Index(name = "idx_sensor_movement", columnList = "movement_id"),
        Index(name = "idx_sensor_sensorname", columnList = "sensor_name"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class SensorInfo : BaseEntity<SensorInfo>() {
    companion object : BaseCompanion<SensorInfo, UUID, QSensorInfo> {
        override val entityClass: Class<SensorInfo> = SensorInfo::class.java
        override val q: QSensorInfo = QSensorInfo.sensorInfo

        fun findByMacAddress(macAddress: String): SensorInfo? {
            return createQuery()
                .where(q.macAddress.eq(macAddress))
                .fetchFirst()
        }
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
    @Column(name = "sensor_name")
    var sensorName: String? = null
}
