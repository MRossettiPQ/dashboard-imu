package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.session.enums.NodeRoleEnum
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull


@Entity
@Table(
    name = "movement_nodes",
    indexes = [
        Index(name = "idx_movement_node_movement", columnList = "movement_id"),
        Index(name = "idx_movement_node_session_node", columnList = "session_node_id"),
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_movement_node", columnNames = ["movement_id", "session_node_id"])
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class MovementNode : BaseEntity<MovementNode>() {
    companion object : BaseCompanion<MovementNode, Int, QMovementNode> {
        override val entityClass: Class<MovementNode> = MovementNode::class.java
        override val q: QMovementNode = QMovementNode.movementNode
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Int? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: NodeRoleEnum? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movement_id", nullable = false)
    var movement: Movement? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_node_id", nullable = false)
    var sessionNode: SessionNode? = null
}