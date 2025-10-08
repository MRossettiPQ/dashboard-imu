package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.exceptions.ApplicationException
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.session.enums.MovementEnum
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.core.Response
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

        fun findByType(type: MovementEnum): MovementType {
            return createQuery()
                .where(q.type.eq(type))
                .fetchFirst() ?: throw ApplicationException("MovementType not found", Response.Status.NOT_FOUND)
        }

    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    override var id: UUID? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, unique = true)
    var type: MovementEnum? = null

    @Column(name = "description", nullable = false)
    var description: MovementEnum? = null
}
