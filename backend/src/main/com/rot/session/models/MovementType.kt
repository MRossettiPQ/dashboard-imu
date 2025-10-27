package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.exceptions.ApplicationException
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.session.enums.MovementEnum
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.core.Response
import org.hibernate.annotations.ColumnTransformer
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
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
    @Column(name = "type", nullable = false)
    var type: MovementEnum? = null

    @Column(name = "description", nullable = false)
    var description: String? = null

    @Column(name = "image_name")
    var imageName: String? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_type_id", nullable = false)
    var procedureType: ProcedureType? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Basic(fetch = FetchType.LAZY)
    @ColumnTransformer(write = "?::jsonb")
    @Column(name = "angle_rule", columnDefinition = "jsonb")
    var angleRule: AngleRule? = null

}

class AngleRule {
    var min: BigDecimal? = null
    var max: BigDecimal? = null
}