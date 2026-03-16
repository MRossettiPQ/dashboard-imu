package com.rot.gonimetry.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.gonimetry.enums.BodyPointEnum
import com.rot.gonimetry.enums.BodyRegionEnum
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull


@Entity
@Table(
    name = "body_points",
    indexes = [
        Index(name = "idx_body_point_region", columnList = "region"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class BodyPoint : BaseEntity<BodyPoint>() {
    companion object : BaseCompanion<BodyPoint, Int, QBodyPoint> {
        override val entityClass: Class<BodyPoint> = BodyPoint::class.java
        override val q: QBodyPoint = QBodyPoint.bodyPoint

        fun findByName(name: String): BodyPoint? {
            return createQuery()
                .where(q.name.eq(name))
                .fetchFirst()
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Int? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, unique = true)
    var code: BodyPointEnum? = null

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "region", nullable = false)
    var region: BodyRegionEnum? = null
}