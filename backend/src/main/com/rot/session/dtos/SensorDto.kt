package com.rot.session.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import com.rot.core.jaxrs.PaginationDto
import com.rot.core.utils.JsonUtils
import com.rot.session.enums.BodySegmentEnum
import com.rot.session.enums.JointEnum
import com.rot.session.enums.PositionEnum
import com.rot.session.enums.SensorOrientationEnum
import com.rot.session.enums.SensorPlacementEnum
import com.rot.session.enums.SensorType
import com.rot.session.models.Sensor
import java.util.*

open class SensorDto {
    var id: UUID? = null
    var ip: String? = null
    var macAddress: String? = null
    var sensorName: String? = null
    var position: PositionEnum? = null
    var type: SensorType = SensorType.GYROSCOPE
    var observation: String? = null

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var measurements = mutableSetOf<MeasurementDto>()

    companion object {
        fun from(sensor: Sensor): SensorDto {
            return JsonUtils.MAPPER.convertValue(sensor, SensorDto::class.java)
        }

        fun from(paginationDto: PaginationDto<Sensor>): PaginationDto<SensorDto> {
            return paginationDto.transform { from(it) }
        }
    }
}

data class SensorConfig(
    val segment: BodySegmentEnum,
    val placement: SensorPlacementEnum,
    val orientation: SensorOrientationEnum,
    val isProximal: Boolean,
    val description: String
)

data class JointSensorConfig(
    val joint: JointEnum,
    val proximalSensor: SensorConfig,
    val distalSensor: SensorConfig,
    val instructions: String
)

object MovementSensorConfigurations {
    val KNEE_RIGHT = JointSensorConfig(
        joint = JointEnum.RIGHT_KNEE,
        proximalSensor = SensorConfig(
            segment = BodySegmentEnum.RIGHT_THIGH,
            placement = SensorPlacementEnum.DISTAL_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = true,
            description = "Coxa direita - face anterior, terço distal (acima do joelho)"
        ),
        distalSensor = SensorConfig(
            segment = BodySegmentEnum.RIGHT_SHANK,
            placement = SensorPlacementEnum.PROXIMAL_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = false,
            description = "Canela direita - face anterior, terço proximal (abaixo do joelho)"
        ),
        instructions = """
            1. Posicionar o sensor da coxa na face anterior, ~10cm acima da patela
            2. Posicionar o sensor da canela na face anterior da tíbia, ~10cm abaixo da patela
            3. Ambos os sensores com eixo Y apontando para baixo (distal)
            4. Paciente em pé, joelho estendido para calibração
        """.trimIndent()
    )

    val KNEE_LEFT = JointSensorConfig(
        joint = JointEnum.LEFT_KNEE,
        proximalSensor = SensorConfig(
            segment = BodySegmentEnum.LEFT_THIGH,
            placement = SensorPlacementEnum.DISTAL_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = true,
            description = "Coxa esquerda - face anterior, terço distal"
        ),
        distalSensor = SensorConfig(
            segment = BodySegmentEnum.LEFT_SHANK,
            placement = SensorPlacementEnum.PROXIMAL_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = false,
            description = "Canela esquerda - face anterior, terço proximal"
        ),
        instructions = """
            1. Posicionar o sensor da coxa na face anterior, ~10cm acima da patela
            2. Posicionar o sensor da canela na face anterior da tíbia, ~10cm abaixo da patela
            3. Ambos os sensores com eixo Y apontando para baixo (distal)
            4. Paciente em pé, joelho estendido para calibração
        """.trimIndent()
    )

    val ANKLE_RIGHT = JointSensorConfig(
        joint = JointEnum.RIGHT_ANKLE,
        proximalSensor = SensorConfig(
            segment = BodySegmentEnum.RIGHT_SHANK,
            placement = SensorPlacementEnum.DISTAL_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = true,
            description = "Canela direita - face anterior, terço distal (acima do tornozelo)"
        ),
        distalSensor = SensorConfig(
            segment = BodySegmentEnum.RIGHT_FOOT,
            placement = SensorPlacementEnum.MIDDLE_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = false,
            description = "Pé direito - dorso do pé, sobre os metatarsos"
        ),
        instructions = """
            1. Posicionar o sensor da canela ~5cm acima do maléolo lateral
            2. Posicionar o sensor do pé no dorso, alinhado com o 3º metatarso
            3. Eixo Y do sensor da canela apontando para baixo
            4. Eixo Y do sensor do pé apontando para os dedos
            5. Paciente em pé, tornozelo em posição neutra para calibração
        """.trimIndent()
    )

    val ANKLE_LEFT = JointSensorConfig(
        joint = JointEnum.LEFT_ANKLE,
        proximalSensor = SensorConfig(
            segment = BodySegmentEnum.LEFT_SHANK,
            placement = SensorPlacementEnum.DISTAL_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = true,
            description = "Canela esquerda - face anterior, terço distal"
        ),
        distalSensor = SensorConfig(
            segment = BodySegmentEnum.LEFT_FOOT,
            placement = SensorPlacementEnum.MIDDLE_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = false,
            description = "Pé esquerdo - dorso do pé"
        ),
        instructions = """
            1. Posicionar o sensor da canela ~5cm acima do maléolo lateral
            2. Posicionar o sensor do pé no dorso, alinhado com o 3º metatarso
            3. Paciente em pé, tornozelo em posição neutra para calibração
        """.trimIndent()
    )

    val HIP_RIGHT = JointSensorConfig(
        joint = JointEnum.RIGHT_HIP,
        proximalSensor = SensorConfig(
            segment = BodySegmentEnum.PELVIS,
            placement = SensorPlacementEnum.LATERAL,
            orientation = SensorOrientationEnum.SUPERIOR,
            isProximal = true,
            description = "Pelve - sobre o sacro ou EIPS (espinha ilíaca póstero-superior)"
        ),
        distalSensor = SensorConfig(
            segment = BodySegmentEnum.RIGHT_THIGH,
            placement = SensorPlacementEnum.PROXIMAL_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = false,
            description = "Coxa direita - face lateral, terço proximal"
        ),
        instructions = """
            1. Posicionar o sensor da pelve centralizado sobre o sacro
            2. Posicionar o sensor da coxa na face lateral, ~10cm abaixo do trocânter maior
            3. Eixo Y do sensor da pelve apontando para cima
            4. Eixo Y do sensor da coxa apontando para baixo
            5. Paciente em pé, quadril em posição neutra para calibração
        """.trimIndent()
    )

    val HIP_LEFT = JointSensorConfig(
        joint = JointEnum.LEFT_HIP,
        proximalSensor = SensorConfig(
            segment = BodySegmentEnum.PELVIS,
            placement = SensorPlacementEnum.LATERAL,
            orientation = SensorOrientationEnum.SUPERIOR,
            isProximal = true,
            description = "Pelve - sobre o sacro"
        ),
        distalSensor = SensorConfig(
            segment = BodySegmentEnum.LEFT_THIGH,
            placement = SensorPlacementEnum.PROXIMAL_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = false,
            description = "Coxa esquerda - face lateral, terço proximal"
        ),
        instructions = """
            1. Posicionar o sensor da pelve centralizado sobre o sacro
            2. Posicionar o sensor da coxa na face lateral, ~10cm abaixo do trocânter maior
            5. Paciente em pé, quadril em posição neutra para calibração
        """.trimIndent()
    )

    // ==================== MEMBROS SUPERIORES ====================

    val ELBOW_RIGHT = JointSensorConfig(
        joint = JointEnum.RIGHT_ELBOW,
        proximalSensor = SensorConfig(
            segment = BodySegmentEnum.RIGHT_UPPER_ARM,
            placement = SensorPlacementEnum.DISTAL_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = true,
            description = "Braço direito - face posterior, terço distal (acima do cotovelo)"
        ),
        distalSensor = SensorConfig(
            segment = BodySegmentEnum.RIGHT_FOREARM,
            placement = SensorPlacementEnum.PROXIMAL_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = false,
            description = "Antebraço direito - face posterior, terço proximal"
        ),
        instructions = """
            1. Posicionar o sensor do braço na face posterior, ~8cm acima do olécrano
            2. Posicionar o sensor do antebraço na face posterior, ~8cm abaixo do olécrano
            3. Ambos os sensores com eixo Y apontando para distal
            4. Paciente com braço ao lado do corpo, cotovelo estendido para calibração
        """.trimIndent()
    )

    val ELBOW_LEFT = JointSensorConfig(
        joint = JointEnum.LEFT_ELBOW,
        proximalSensor = SensorConfig(
            segment = BodySegmentEnum.LEFT_UPPER_ARM,
            placement = SensorPlacementEnum.DISTAL_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = true,
            description = "Braço esquerdo - face posterior, terço distal"
        ),
        distalSensor = SensorConfig(
            segment = BodySegmentEnum.LEFT_FOREARM,
            placement = SensorPlacementEnum.PROXIMAL_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = false,
            description = "Antebraço esquerdo - face posterior, terço proximal"
        ),
        instructions = """
            1. Posicionar o sensor do braço na face posterior, ~8cm acima do olécrano
            2. Posicionar o sensor do antebraço na face posterior, ~8cm abaixo do olécrano
            3. Paciente com braço ao lado do corpo, cotovelo estendido para calibração
        """.trimIndent()
    )

    val WRIST_RIGHT = JointSensorConfig(
        joint = JointEnum.RIGHT_WRIST,
        proximalSensor = SensorConfig(
            segment = BodySegmentEnum.RIGHT_FOREARM,
            placement = SensorPlacementEnum.DISTAL_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = true,
            description = "Antebraço direito - face dorsal, terço distal"
        ),
        distalSensor = SensorConfig(
            segment = BodySegmentEnum.RIGHT_HAND,
            placement = SensorPlacementEnum.MIDDLE_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = false,
            description = "Mão direita - dorso da mão, sobre os metacarpos"
        ),
        instructions = """
            1. Posicionar o sensor do antebraço ~5cm acima do punho
            2. Posicionar o sensor da mão no dorso, alinhado com o 3º metacarpo
            3. Eixo Y apontando para os dedos
            4. Punho em posição neutra para calibração
        """.trimIndent()
    )

    val WRIST_LEFT = JointSensorConfig(
        joint = JointEnum.LEFT_WRIST,
        proximalSensor = SensorConfig(
            segment = BodySegmentEnum.LEFT_FOREARM,
            placement = SensorPlacementEnum.DISTAL_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = true,
            description = "Antebraço esquerdo - face dorsal, terço distal"
        ),
        distalSensor = SensorConfig(
            segment = BodySegmentEnum.LEFT_HAND,
            placement = SensorPlacementEnum.MIDDLE_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = false,
            description = "Mão esquerda - dorso da mão"
        ),
        instructions = """
            1. Posicionar o sensor do antebraço ~5cm acima do punho
            2. Posicionar o sensor da mão no dorso, alinhado com o 3º metacarpo
            3. Punho em posição neutra para calibração
        """.trimIndent()
    )

    val SHOULDER_RIGHT = JointSensorConfig(
        joint = JointEnum.RIGHT_SHOULDER,
        proximalSensor = SensorConfig(
            segment = BodySegmentEnum.UPPER_SPINE,
            placement = SensorPlacementEnum.POSTERIOR,
            orientation = SensorOrientationEnum.SUPERIOR,
            isProximal = true,
            description = "Coluna torácica - entre as escápulas (T3-T4)"
        ),
        distalSensor = SensorConfig(
            segment = BodySegmentEnum.RIGHT_UPPER_ARM,
            placement = SensorPlacementEnum.PROXIMAL_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = false,
            description = "Braço direito - face lateral, terço proximal"
        ),
        instructions = """
            1. Posicionar o sensor do tronco entre as escápulas (nível T3-T4)
            2. Posicionar o sensor do braço na face lateral, ~8cm abaixo do acrômio
            3. Eixo Y do sensor do tronco apontando para cima
            4. Eixo Y do sensor do braço apontando para baixo
            5. Paciente em pé, braço ao lado do corpo para calibração
        """.trimIndent()
    )

    val SHOULDER_LEFT = JointSensorConfig(
        joint = JointEnum.LEFT_SHOULDER,
        proximalSensor = SensorConfig(
            segment = BodySegmentEnum.UPPER_SPINE,
            placement = SensorPlacementEnum.POSTERIOR,
            orientation = SensorOrientationEnum.SUPERIOR,
            isProximal = true,
            description = "Coluna torácica - entre as escápulas"
        ),
        distalSensor = SensorConfig(
            segment = BodySegmentEnum.LEFT_UPPER_ARM,
            placement = SensorPlacementEnum.PROXIMAL_THIRD,
            orientation = SensorOrientationEnum.DISTAL,
            isProximal = false,
            description = "Braço esquerdo - face lateral, terço proximal"
        ),
        instructions = """
            1. Posicionar o sensor do tronco entre as escápulas (nível T3-T4)
            2. Posicionar o sensor do braço na face lateral, ~8cm abaixo do acrômio
            3. Paciente em pé, braço ao lado do corpo para calibração
        """.trimIndent()
    )

    // ==================== COLUNA ====================

    val CERVICAL = JointSensorConfig(
        joint = JointEnum.CERVICAL,
        proximalSensor = SensorConfig(
            segment = BodySegmentEnum.UPPER_SPINE,
            placement = SensorPlacementEnum.POSTERIOR,
            orientation = SensorOrientationEnum.SUPERIOR,
            isProximal = true,
            description = "Coluna torácica superior - nível C7-T1"
        ),
        distalSensor = SensorConfig(
            segment = BodySegmentEnum.NECK,
            placement = SensorPlacementEnum.POSTERIOR,
            orientation = SensorOrientationEnum.SUPERIOR,
            isProximal = false,
            description = "Pescoço - base do crânio/occipital"
        ),
        instructions = """
            1. Posicionar o sensor inferior sobre o processo espinhoso de C7
            2. Posicionar o sensor superior na base do crânio (protuberância occipital)
            3. Ambos os sensores com eixo Y apontando para cima
            4. Paciente sentado, olhando para frente para calibração
        """.trimIndent()
    )

    val HEAD_NECK = JointSensorConfig(
        joint = JointEnum.ATLANTO_OCCIPITAL,
        proximalSensor = SensorConfig(
            segment = BodySegmentEnum.NECK,
            placement = SensorPlacementEnum.POSTERIOR,
            orientation = SensorOrientationEnum.SUPERIOR,
            isProximal = true,
            description = "Base do crânio"
        ),
        distalSensor = SensorConfig(
            segment = BodySegmentEnum.HEAD,
            placement = SensorPlacementEnum.POSTERIOR,
            orientation = SensorOrientationEnum.SUPERIOR,
            isProximal = false,
            description = "Cabeça - região parietal posterior"
        ),
        instructions = """
            1. Posicionar o sensor inferior na base do crânio
            2. Posicionar o sensor superior no topo da cabeça (vértex)
            3. Ambos os sensores com eixo Y apontando para cima
            4. Paciente sentado, olhando para frente para calibração
        """.trimIndent()
    )

    val LUMBAR = JointSensorConfig(
        joint = JointEnum.LUMBOSACRAL,
        proximalSensor = SensorConfig(
            segment = BodySegmentEnum.PELVIS,
            placement = SensorPlacementEnum.POSTERIOR,
            orientation = SensorOrientationEnum.SUPERIOR,
            isProximal = true,
            description = "Sacro - S1-S2"
        ),
        distalSensor = SensorConfig(
            segment = BodySegmentEnum.LOWER_SPINE,
            placement = SensorPlacementEnum.POSTERIOR,
            orientation = SensorOrientationEnum.SUPERIOR,
            isProximal = false,
            description = "Coluna lombar - L1-L2"
        ),
        instructions = """
            1. Posicionar o sensor inferior sobre o sacro (S1-S2)
            2. Posicionar o sensor superior sobre L1-L2
            3. Ambos os sensores com eixo Y apontando para cima
            4. Paciente em pé, postura neutra para calibração
        """.trimIndent()
    )

    val THORACIC = JointSensorConfig(
        joint = JointEnum.THORACOLUMBAR,
        proximalSensor = SensorConfig(
            segment = BodySegmentEnum.LOWER_SPINE,
            placement = SensorPlacementEnum.POSTERIOR,
            orientation = SensorOrientationEnum.SUPERIOR,
            isProximal = true,
            description = "Coluna lombar - L1-L2"
        ),
        distalSensor = SensorConfig(
            segment = BodySegmentEnum.UPPER_SPINE,
            placement = SensorPlacementEnum.POSTERIOR,
            orientation = SensorOrientationEnum.SUPERIOR,
            isProximal = false,
            description = "Coluna torácica - T6-T7"
        ),
        instructions = """
            1. Posicionar o sensor inferior sobre L1-L2
            2. Posicionar o sensor superior sobre T6-T7
            3. Ambos os sensores com eixo Y apontando para cima
            4. Paciente em pé, postura neutra para calibração
        """.trimIndent()
    )

    fun getAllConfigurations(): List<JointSensorConfig> = listOf(
        // Membros inferiores
        KNEE_RIGHT, KNEE_LEFT,
        ANKLE_RIGHT, ANKLE_LEFT,
        HIP_RIGHT, HIP_LEFT,
        // Membros superiores
        ELBOW_RIGHT, ELBOW_LEFT,
        WRIST_RIGHT, WRIST_LEFT,
        SHOULDER_RIGHT, SHOULDER_LEFT,
        // Coluna
        CERVICAL, HEAD_NECK, LUMBAR, THORACIC
    )

    fun getByJoint(joint: JointEnum): JointSensorConfig? {
        return getAllConfigurations().find { it.joint == joint }
    }

    fun getBySegment(segment: BodySegmentEnum): List<JointSensorConfig> {
        return getAllConfigurations().filter {
            it.proximalSensor.segment == segment || it.distalSensor.segment == segment
        }
    }
}


data class EulerAngles(
    val flexionExtension: Double,    // Rotação em X (plano sagital)
    val abductionAdduction: Double,  // Rotação em Y (plano frontal)
    val rotation: Double             // Rotação em Z (plano transversal)
) {
    fun toMap(): Map<String, Double> = mapOf(
        "flexionExtension" to flexionExtension,
        "abductionAdduction" to abductionAdduction,
        "rotation" to rotation
    )
}
