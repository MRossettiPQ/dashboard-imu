package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.gonimetry.models.MovementType
import com.rot.gonimetry.enums.MovementEnum
import com.rot.session.enums.ExecutionModeEnum
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.util.*


@Entity
@Table(
    name = "movements",
    indexes = [
//        Index(name = "idx_movement_articulation", columnList = "articulation_id"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class Movement : BaseEntity<Movement>() {
    companion object : BaseCompanion<Movement, Int, QMovement> {
        override val entityClass: Class<Movement> = Movement::class.java
        override val q: QMovement = QMovement.movement
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Int? = null

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "observation", columnDefinition = "TEXT")
    var observation: String? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: MovementEnum? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "execution_mode", nullable = false)
    var executionMode: ExecutionModeEnum? = null

    @Column(name = "start_angle")
    var startAngle: Double? = null

    @Column(name = "end_angle")
    var endAngle: Double? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movement_type_id", nullable = false)
    var movementType: MovementType? = null
}