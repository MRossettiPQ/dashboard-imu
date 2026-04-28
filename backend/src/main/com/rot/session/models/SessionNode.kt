package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.session.enums.BodyRegionEnum
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull


@Entity
@Table(
    name = "session_nodes",
    indexes = [
        Index(name = "idx_session_nodes_session", columnList = "session_id"),
//        Index(name = "idx_session_node_body_point", columnList = "body_point_id"),
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_session_nodes_region", columnNames = ["session_id", "region"])
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
    @Column(name = "body_region", nullable = false)
    lateinit var region: BodyRegionEnum

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    var session: Session? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sessionNode", cascade = [CascadeType.ALL])
    var nodeSensors = mutableSetOf<NodeSensor>()
}