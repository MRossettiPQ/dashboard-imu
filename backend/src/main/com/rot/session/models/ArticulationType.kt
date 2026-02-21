package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.session.enums.ArticulationEnum
import com.rot.session.enums.BodySideEnum
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.util.*


@Entity
@Table(
    name = "articulation_types",
    indexes = [
        Index(name = "idx_articulation_types_type", columnList = "type"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class ArticulationType : BaseEntity<ArticulationType>() {
    companion object : BaseCompanion<ArticulationType, Int, QProcedureType> {
        override val entityClass: Class<ArticulationType> = ArticulationType::class.java
        override val q: QProcedureType = QProcedureType.procedureType
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Int? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    var side: BodySideEnum = BodySideEnum.RIGHT

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: ArticulationEnum? = null

    @Column(name = "description", nullable = false)
    var description: String? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "articulationType", cascade = [CascadeType.ALL])
    var movementsTypes = mutableSetOf<MovementType>()
}
