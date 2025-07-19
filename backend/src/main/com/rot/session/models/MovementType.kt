package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.session.enums.MovementEnum
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.util.*


@Entity
@Table(
    name = "movement_types",
    indexes = [
        Index(name = "idx_movementtype_type", columnList = "type"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class MovementType : BaseEntity<MovementType>() {
    companion object : BaseCompanion<MovementType, UUID, QMovementType> {
        override val entityClass: Class<MovementType> = MovementType::class.java
        override val q: QMovementType = QMovementType.movementType
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    override var id: UUID? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: MovementEnum? = null

    @Column(name = "description", nullable = false)
    var description: MovementEnum? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "type", cascade = [CascadeType.ALL])
    var movements = mutableSetOf<Movement>()

}
