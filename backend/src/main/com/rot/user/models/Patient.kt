package com.rot.user.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*


@Entity
@Table(
    name = "patients",
    indexes = [
        Index(name = "idx_patient_cpf", columnList = "cpf"),
        Index(name = "idx_patient_active", columnList = "active"),
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

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "user_id")
    var user: User? = null

}
