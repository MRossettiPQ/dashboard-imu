package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.session.enums.ProcedureEnum
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.util.*


@Entity
@Table(
    name = "procedure_types",
    indexes = [
        Index(name = "idx_proceduretype_type", columnList = "type"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class ProcedureType : BaseEntity<ProcedureType>() {
    companion object : BaseCompanion<ProcedureType, UUID, QProcedureType> {
        override val entityClass: Class<ProcedureType> = ProcedureType::class.java
        override val q: QProcedureType = QProcedureType.procedureType
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    override var id: UUID? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: ProcedureEnum? = null

    @Column(name = "description", nullable = false)
    var description: String? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "procedureType", cascade = [CascadeType.ALL])
    var movementsTypes = mutableSetOf<MovementType>()
}
