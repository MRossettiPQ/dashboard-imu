package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.gonimetry.models.BodyPoint
import com.rot.session.enums.BodySideEnum
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull


@Entity
@Table(
    name = "session_nodes",
    indexes = [
        Index(name = "idx_session_node_session", columnList = "session_id"),
        Index(name = "idx_session_node_body_point", columnList = "body_point_id"),
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_session_node_point_side", columnNames = ["session_id", "body_point_id", "side"])
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class SessionNode : BaseEntity<SessionNode>() {
    companion object : BaseCompanion<SessionNode, Int, QSessionNode> {
        override val entityClass: Class<SessionNode> = SessionNode::class.java
        override val q: QSessionNode = QSessionNode.sessionNode
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Int? = null

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "observation", columnDefinition = "TEXT")
    var observation: String? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    var side: BodySideEnum = BodySideEnum.RIGHT

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    var session: Session? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "body_point_id", nullable = false)
    var bodyPoint: BodyPoint? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sessionNode", cascade = [CascadeType.ALL])
    var nodeSensors = mutableSetOf<NodeSensor>()
}