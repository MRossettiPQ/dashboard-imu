package com.rot.gonimetry.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.gonimetry.enums.ArticulationEnum
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull


@Entity
@Table(
    name = "articulation_types",
    indexes = [
        Index(name = "idx_articulation_types_type", columnList = "type"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class ArticulationType : BaseEntity<ArticulationType>() {
    companion object : BaseCompanion<ArticulationType, Int, QArticulationType> {
        override val entityClass: Class<ArticulationType> = ArticulationType::class.java
        override val q: QArticulationType = QArticulationType.articulationType
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Int? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: ArticulationEnum? = null

    @Column(name = "description", nullable = false)
    var description: String? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "articulationType", cascade = [CascadeType.ALL])
    var movementsTypes = mutableSetOf<MovementType>()
}
