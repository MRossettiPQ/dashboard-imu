package com.rot.user.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import com.rot.core.jaxrs.Pagination
import com.rot.core.utils.JsonUtils
import com.rot.user.models.Patient
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

open class PatientDto {
    var id: UUID? = null
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var createdAt: OffsetDateTime? = null
    var birthday: LocalDate? = null
    var cpf: String? = null
    var phone: String? = null
    var cellphone: String? = null
    var stature: BigDecimal? = null
    var user: UserDto? = null

    companion object {
        fun from(entity: Patient): PatientDto {
            val dto = JsonUtils.MAPPER.convertValue(entity, PatientDto::class.java)
            dto.user = UserDto.from(entity.user!!)
            return dto
        }
        fun from(pagination: Pagination<Patient>) : Pagination<PatientDto> {
            return pagination.transform { from(it) }
        }
    }
}