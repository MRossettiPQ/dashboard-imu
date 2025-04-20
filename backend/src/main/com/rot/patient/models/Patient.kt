package com.rot.patient.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.user.enums.UserRole
import com.rot.user.models.User
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*


@Entity
@Table(
    name = "patients",
    indexes = [
        Index(name = "idx_patient_active", columnList = "active"),
        Index(name = "idx_patient_cpf", columnList = "cpf"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class Patient : BaseEntity<Patient>() {
    companion object : BaseCompanion<Patient, UUID, QPatient> {
        override val entityClass: Class<Patient> = Patient::class.java
        override val q: QPatient = QPatient.patient
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    override var id: UUID? = null

    @Column(name = "active", nullable = false)
    var active: Boolean = false

    @Column(name = "cpf")
    var cpf: String? = null

    @Column(name = "phone")
    var phone: String? = null

    @Column(name = "birthday")
    var birthday: LocalDateTime? = null

    @Column(name = "stature")
    var stature: BigDecimal? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null

}
