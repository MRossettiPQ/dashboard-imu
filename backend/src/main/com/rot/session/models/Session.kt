package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.session.enums.SessionType
import com.rot.user.models.User
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.*


@Entity
@Table(
    name = "sessions",
    indexes = [
        Index(name = "idx_session_type", columnList = "type"),
        Index(name = "idx_session_patient", columnList = "patient_id"),
        Index(name = "idx_session_physiotherapist", columnList = "physiotherapist_id"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class Session : BaseEntity<Session>() {
    companion object : BaseCompanion<Session, UUID, QSession> {
        override val entityClass: Class<Session> = Session::class.java
        override val q: QSession = QSession.session
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    override var id: UUID? = null

    @NotNull
    @Column(name = "date", nullable = false, updatable = false)
    var date: LocalDateTime = LocalDateTime.now()

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: SessionType = SessionType.REAL

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "observation", columnDefinition = "TEXT")
    var observation: String? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    var patient: User? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "physiotherapist_id")
    var physiotherapist: User? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "session", cascade = [CascadeType.ALL])
    var procedures = mutableSetOf<Procedure>()
}
