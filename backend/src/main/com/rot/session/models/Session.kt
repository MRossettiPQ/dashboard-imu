package com.rot.session.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.session.enums.ProcedureType
import com.rot.session.enums.SessionType
import com.rot.user.enums.UserRole
import com.rot.user.models.User
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.*


@Entity
@Table(
    name = "sessions",
    indexes = [
        Index(name = "idx_session_type", columnList = "type"),
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
    override var id: UUID? = null

    @NotNull
    @Column(name = "date", nullable = false)
    var date: LocalDateTime = LocalDateTime.now()

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: SessionType = SessionType.REAL

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "procedure", nullable = false)
    var procedure: ProcedureType? = null

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "observation", columnDefinition = "TEXT")
    var observation: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    var patient: User? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "physiotherapist_id")
    var physiotherapist: User? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "session", cascade = [CascadeType.ALL])
    var movements = mutableSetOf<Movement>()
}
