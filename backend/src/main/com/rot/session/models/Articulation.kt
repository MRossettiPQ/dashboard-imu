package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.gonimetry.models.ArticulationType
import com.rot.session.enums.BodySideEnum
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.util.*


@Entity
@Table(
    name = "articulations",
    indexes = [
        Index(name = "idx_articulation_type", columnList = "type_id"),
        Index(name = "idx_articulation_session", columnList = "session_id"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class Articulation : BaseEntity<Articulation>() {
    companion object : BaseCompanion<Articulation, Int, QArticulation> {
        override val entityClass: Class<Articulation> = Articulation::class.java
        override val q: QArticulation = QArticulation.articulation
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Int? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "session_id", nullable = false)
    var session: Session? = null

    @Column(name = "description")
    var description: String? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    var side: BodySideEnum = BodySideEnum.RIGHT

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    var type: ArticulationType? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "articulation", cascade = [CascadeType.ALL])
    var movements = mutableSetOf<Movement>()
}