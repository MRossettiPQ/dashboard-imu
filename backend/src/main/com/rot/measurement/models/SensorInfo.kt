package com.rot.measurement.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

@Entity
@Table(
    name = "sensor_infos",
    indexes = [
        Index(name = "idx_sensorinfo_macaddress", columnList = "mac_address"),
        Index(name = "idx_sensorinfo_sensorname", columnList = "sensor_name"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class SensorInfo : BaseEntity<SensorInfo>() {
    companion object : BaseCompanion<SensorInfo, Int, QSensorInfo> {
        override val entityClass: Class<SensorInfo> = SensorInfo::class.java
        override val q: QSensorInfo = QSensorInfo.sensorInfo

        fun findByMacAddress(macAddress: String): SensorInfo? {
            return createQuery()
                .where(q.macAddress.eq(macAddress))
                .fetchFirst()
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Int? = null

    @NotNull
    @NotEmpty
    @Column(name = "mac_address")
    var macAddress: String? = null

    @NotNull
    @NotEmpty
    @Column(name = "sensor_name")
    var sensorName: String? = null
}