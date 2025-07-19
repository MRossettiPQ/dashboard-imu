package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.util.*


@Entity
@Table(
    name = "movements",
    indexes = [
        Index(name = "idx_movement_type", columnList = "type"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class Movement : BaseEntity<Movement>() {
    companion object : BaseCompanion<Movement, UUID, QMovement> {
        override val entityClass: Class<Movement> = Movement::class.java
        override val q: QMovement = QMovement.movement
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    override var id: UUID? = null

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "observation", columnDefinition = "TEXT")
    var observation: String? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "procedure_id", nullable = false)
    var procedure: Procedure? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "type_id", nullable = false)
    var type: MovementType? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "movement", cascade = [CascadeType.ALL])
    var sensors = mutableSetOf<Sensor>()
}
