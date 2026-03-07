package com.rot.gonimetry.models

import com.querydsl.core.annotations.Config
import com.rot.core.exceptions.ApplicationException
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.file.models.FileStorage
import com.rot.gonimetry.enums.MovementEnum
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.core.Response
import org.hibernate.annotations.ColumnTransformer
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal


@Entity
@Table(
    name = "movement_types",
    indexes = [
        Index(name = "idx_movementtype_type", columnList = "type"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class MovementType : BaseEntity<MovementType>() {
    companion object : BaseCompanion<MovementType, Int, QMovementType> {
        override val entityClass: Class<MovementType> = MovementType::class.java
        override val q: QMovementType = QMovementType.movementType

        fun findByType(type: MovementEnum): MovementType {
            return createQuery()
                .where(q.type.eq(type))
                .fetchFirst() ?: throw ApplicationException("MovementType not found", Response.Status.NOT_FOUND)
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    override var id: Int? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: MovementEnum? = null

    @Column(name = "description", nullable = false)
    var description: String? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    var image: FileStorage? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_type_id", nullable = false)
    var articulationType: ArticulationType? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Basic(fetch = FetchType.LAZY)
    @ColumnTransformer(write = "?::jsonb")
    @Column(name = "angle_rule", columnDefinition = "jsonb")
    var angleRule: AngleRule? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Basic(fetch = FetchType.LAZY)
    @ColumnTransformer(write = "?::jsonb")
    @Column(name = "goniometry_procedure", columnDefinition = "jsonb")
    var goniometryProcedure: GoniometryProcedure? = null
}

class AngleRule {
    var min: BigDecimal? = null
    var max: BigDecimal? = null
}

class GoniometryProcedure {
    /** Posição ideal do paciente para a medição */
    var idealPosition: String? = null

    /** Descrição de onde colocar o braço fixo do goniômetro */
    var fixedArm: String? = null

    /** Descrição de onde colocar o braço móvel do goniômetro */
    var movableArm: String? = null

    /** Ponto de referência do eixo do goniômetro */
    var axis: String? = null

    /** Observações e cuidados durante a medição */
    var observation: String? = null

    /** Nome da figura de referência no manual */
    var figureName: String? = null
}