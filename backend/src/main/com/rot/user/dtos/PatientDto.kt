package com.rot.user.dtos

import com.rot.user.models.Patient
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class PatientDto {
    var id: UUID? = null
    var createdAt: LocalDateTime? = null
    var birthday: LocalDateTime? = null
    var cpf: String? = null
    var phone: String? = null
    var stature: BigDecimal? = null
    var user: UserDto? = null

    companion object {
        fun from(patient: Patient): PatientDto {
           return PatientDto().apply {
               this.id = patient.id
               this.createdAt = patient.createdAt
               this.cpf = patient.cpf
               this.phone = patient.phone
               this.stature = patient.stature
               this.birthday = patient.birthday
               this.user = UserDto.from(patient.user!!)
           }
        }
    }
}