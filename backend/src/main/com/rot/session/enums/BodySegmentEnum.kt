package com.rot.session.enums

enum class BodySegmentEnum(val description: String, val parentSegment: BodySegmentEnum? = null) {
    // Coluna
    PELVIS("Pelve/Quadril", null),
    LOWER_SPINE("Coluna Inferior/Lombar", PELVIS),
    UPPER_SPINE("Coluna Superior/Torácica", LOWER_SPINE),

    // Cabeça e Pescoço
    NECK("Pescoço", UPPER_SPINE),
    HEAD("Cabeça", NECK),

    // Membro Superior Direito
    RIGHT_SHOULDER("Ombro Direito", UPPER_SPINE),
    RIGHT_UPPER_ARM("Braço Direito", RIGHT_SHOULDER),
    RIGHT_FOREARM("Antebraço Direito", RIGHT_UPPER_ARM),
    RIGHT_HAND("Mão Direita", RIGHT_FOREARM),

    // Membro Superior Esquerdo
    LEFT_SHOULDER("Ombro Esquerdo", UPPER_SPINE),
    LEFT_UPPER_ARM("Braço Esquerdo", LEFT_SHOULDER),
    LEFT_FOREARM("Antebraço Esquerdo", LEFT_UPPER_ARM),
    LEFT_HAND("Mão Esquerda", LEFT_FOREARM),

    // Membro Inferior Direito
    RIGHT_THIGH("Coxa Direita", PELVIS),
    RIGHT_SHANK("Canela/Perna Direita", RIGHT_THIGH),
    RIGHT_FOOT("Pé Direito", RIGHT_SHANK),

    // Membro Inferior Esquerdo
    LEFT_THIGH("Coxa Esquerda", PELVIS),
    LEFT_SHANK("Canela/Perna Esquerda", LEFT_THIGH),
    LEFT_FOOT("Pé Esquerdo", LEFT_SHANK);

    /**
     * Retorna o segmento distal (mais distante do tronco)
     */
    fun getDistalSegments(): List<BodySegmentEnum> {
        return entries.filter { it.parentSegment == this }
    }
}

enum class SensorPlacementEnum(val description: String) {
    PROXIMAL_THIRD("Terço proximal do segmento"),
    MIDDLE_THIRD("Terço médio do segmento"),
    DISTAL_THIRD("Terço distal do segmento"),
    OVER_JOINT("Sobre a articulação"),
    LATERAL("Face lateral"),
    MEDIAL("Face medial"),
    ANTERIOR("Face anterior"),
    POSTERIOR("Face posterior")
}

enum class SensorOrientationEnum(val description: String) {
    PROXIMAL("Eixo Y apontando para proximal (em direção ao tronco)"),
    DISTAL("Eixo Y apontando para distal (em direção à extremidade)"),
    SUPERIOR("Eixo Y apontando para cima"),
    ANTERIOR("Eixo Y apontando para frente"),
    LATERAL("Eixo Y apontando lateralmente")
}

enum class AxisEnum(val description: String) {
    // Movimentos no plano sagital (flexão/extensão)
    FLEXION_EXTENSION("Flexão/Extensão"),
    DORSIFLEXION_PLANTARFLEXION("Dorsiflexão/Flexão Plantar"),

    // Movimentos no plano frontal
    ABDUCTION_ADDUCTION("Abdução/Adução"),
    LATERAL_FLEXION("Flexão Lateral"),
    INVERSION_EVERSION("Inversão/Eversão"),
    RADIAL_ULNAR_DEVIATION("Desvio Radial/Ulnar"),

    // Movimentos no plano transversal
    ROTATION("Rotação Interna/Externa"),
    PRONATION_SUPINATION("Pronação/Supinação")
}

enum class JointEnum(
    val description: String,
    val proximalSegment: BodySegmentEnum,  // Segmento mais próximo do tronco
    val distalSegment: BodySegmentEnum,    // Segmento mais distante do tronco
    val primaryAxis: AxisEnum,             // Eixo principal de movimento
    val secondaryAxis: AxisEnum? = null    // Eixo secundário (se houver)
) {
    // Cabeça e Pescoço
    ATLANTO_OCCIPITAL(
        description = "Atlanto-occipital (Cabeça-Pescoço)",
        proximalSegment = BodySegmentEnum.NECK,
        distalSegment = BodySegmentEnum.HEAD,
        primaryAxis = AxisEnum.FLEXION_EXTENSION,
        secondaryAxis = AxisEnum.LATERAL_FLEXION
    ),

    CERVICAL(
        description = "Cervical (Pescoço-Coluna)",
        BodySegmentEnum.UPPER_SPINE, BodySegmentEnum.NECK,
        AxisEnum.FLEXION_EXTENSION, AxisEnum.ROTATION
    ),

    // Coluna
    THORACOLUMBAR(
        description = "Toracolombar",
        BodySegmentEnum.LOWER_SPINE, BodySegmentEnum.UPPER_SPINE,
        AxisEnum.FLEXION_EXTENSION, AxisEnum.ROTATION
    ),

    LUMBOSACRAL(
        description = "Lombossacral",
        BodySegmentEnum.PELVIS, BodySegmentEnum.LOWER_SPINE,
        AxisEnum.FLEXION_EXTENSION, AxisEnum.LATERAL_FLEXION
    ),

    // Ombro
    RIGHT_SHOULDER(
        description = "Ombro Direito",
        BodySegmentEnum.UPPER_SPINE, BodySegmentEnum.RIGHT_UPPER_ARM,
        AxisEnum.FLEXION_EXTENSION, AxisEnum.ABDUCTION_ADDUCTION
    ),

    LEFT_SHOULDER(
        description = "Ombro Esquerdo",
        BodySegmentEnum.UPPER_SPINE, BodySegmentEnum.LEFT_UPPER_ARM,
        AxisEnum.FLEXION_EXTENSION, AxisEnum.ABDUCTION_ADDUCTION
    ),

    // Cotovelo
    RIGHT_ELBOW(
        description = "Cotovelo Direito",
        BodySegmentEnum.RIGHT_UPPER_ARM, BodySegmentEnum.RIGHT_FOREARM,
        AxisEnum.FLEXION_EXTENSION, AxisEnum.PRONATION_SUPINATION
    ),

    LEFT_ELBOW(
        description = "Cotovelo Esquerdo",
        BodySegmentEnum.LEFT_UPPER_ARM, BodySegmentEnum.LEFT_FOREARM,
        AxisEnum.FLEXION_EXTENSION, AxisEnum.PRONATION_SUPINATION
    ),

    // Punho
    RIGHT_WRIST(
        description = "Punho Direito",
        BodySegmentEnum.RIGHT_FOREARM, BodySegmentEnum.RIGHT_HAND,
        AxisEnum.FLEXION_EXTENSION, AxisEnum.RADIAL_ULNAR_DEVIATION
    ),

    LEFT_WRIST(
        description = "Punho Esquerdo",
        BodySegmentEnum.LEFT_FOREARM, BodySegmentEnum.LEFT_HAND,
        AxisEnum.FLEXION_EXTENSION, AxisEnum.RADIAL_ULNAR_DEVIATION
    ),

    // Quadril
    RIGHT_HIP(
        description = "Quadril Direito",
        BodySegmentEnum.PELVIS, BodySegmentEnum.RIGHT_THIGH,
        AxisEnum.FLEXION_EXTENSION, AxisEnum.ABDUCTION_ADDUCTION
    ),

    LEFT_HIP(
        description = "Quadril Esquerdo",
        BodySegmentEnum.PELVIS, BodySegmentEnum.LEFT_THIGH,
        AxisEnum.FLEXION_EXTENSION, AxisEnum.ABDUCTION_ADDUCTION
    ),

    // Joelho
    RIGHT_KNEE(
        description = "Joelho Direito",
        BodySegmentEnum.RIGHT_THIGH, BodySegmentEnum.RIGHT_SHANK,
        AxisEnum.FLEXION_EXTENSION, null
    ),

    LEFT_KNEE(
        description = "Joelho Esquerdo",
        BodySegmentEnum.LEFT_THIGH, BodySegmentEnum.LEFT_SHANK,
        AxisEnum.FLEXION_EXTENSION, null
    ),

    // Tornozelo
    RIGHT_ANKLE(
        description = "Tornozelo Direito",
        BodySegmentEnum.RIGHT_SHANK, BodySegmentEnum.RIGHT_FOOT,
        AxisEnum.DORSIFLEXION_PLANTARFLEXION, AxisEnum.INVERSION_EVERSION
    ),

    LEFT_ANKLE(
        description = "Tornozelo Esquerdo",
        BodySegmentEnum.LEFT_SHANK, BodySegmentEnum.LEFT_FOOT,
        AxisEnum.DORSIFLEXION_PLANTARFLEXION, AxisEnum.INVERSION_EVERSION
    );

    companion object {
        fun findBySegments(proximal: BodySegmentEnum, distal: BodySegmentEnum): JointEnum? {
            return entries.find {
                it.proximalSegment == proximal && it.distalSegment == distal
            }
        }
    }
}