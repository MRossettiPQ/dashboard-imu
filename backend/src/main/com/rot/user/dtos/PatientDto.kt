package com.rot.user.dtos

import com.rot.core.jaxrs.ContentDto
import com.rot.core.jaxrs.Pagination
import com.rot.core.utils.JsonUtils
import com.rot.user.models.Patient
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

open class PatientDto {
    var id: UUID? = null
    var createdAt: LocalDateTime? = null
    var birthday: LocalDateTime? = null
    var cpf: String? = null
    var phone: String? = null
    var stature: BigDecimal? = null
    var user: UserDto? = null

    companion object {
        fun from(entity: Patient): PatientDto {
            val dto = JsonUtils.MAPPER.convertValue(entity, PatientDto::class.java)
            println("Println patient dto: $dto")
            dto.user = UserDto.from(entity.user!!)
            return dto
        }
        fun from(pagination: Pagination<Patient>) : Pagination<PatientDto> {
            return pagination.transform { from(it) }
        }
    }
}

@Schema(description = "Resposta com dados do paciente")
class PatientResponse : ContentDto<PatientDto>()