package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.session.enums.ProcedureType
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.util.*


@Entity
@Table(
    name = "procedures",
    indexes = [
        Index(name = "idx_movement_type", columnList = "type"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class Procedure : BaseEntity<Procedure>() {
    companion object : BaseCompanion<Procedure, UUID, QProcedure> {
        override val entityClass: Class<Procedure> = Procedure::class.java
        override val q: QProcedure = QProcedure.procedure
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    override var id: UUID? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "session_id", nullable = false)
    var session: Session? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: ProcedureType? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "procedure", cascade = [CascadeType.ALL])
    var movements = mutableSetOf<Movement>()
}
