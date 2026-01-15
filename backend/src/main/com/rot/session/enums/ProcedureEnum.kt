package com.rot.session.enums

/**
 * Procedimentos/Articulações disponíveis
 * Cada procedimento define quais segmentos corporais (proximal e distal) formam a articulação
 */
enum class ProcedureEnum(
    val description: String,
    val proximalSegment: BodySegmentEnum?,
    val distalSegment: BodySegmentEnum?,
    val movements: Set<MovementEnum> = emptySet()
) {
    SIMPLE(
        description = "Simple",
        proximalSegment = null,
        distalSegment = null
    ),

    // ============ OMBRO ============
    SHOULDER(
        description = "Shoulder",
        proximalSegment = BodySegmentEnum.THORAX,
        distalSegment = BodySegmentEnum.RIGHT_UPPER_ARM,
        movements = setOf(
            MovementEnum.FLEXION,
            MovementEnum.EXTENSION,
            MovementEnum.ABDUCTION,
            MovementEnum.ADDUCTION,
            MovementEnum.INTERNAL_ROTATION,
            MovementEnum.EXTERNAL_ROTATION
        )
    ),

    // ============ COTOVELO ============
    ELBOW(
        description = "Elbow",
        proximalSegment = BodySegmentEnum.RIGHT_UPPER_ARM,
        distalSegment = BodySegmentEnum.RIGHT_FOREARM,
        movements = setOf(
            MovementEnum.FLEXION,
            MovementEnum.EXTENSION
        )
    ),

    // ============ RADIOULNAR (Pronação/Supinação) ============
    RADIOULNAR(
        description = "Radioulnar",
        proximalSegment = BodySegmentEnum.RIGHT_UPPER_ARM,
        distalSegment = BodySegmentEnum.RIGHT_FOREARM,
        movements = setOf(
            MovementEnum.PRONATION,
            MovementEnum.SUPINATION
        )
    ),

    // ============ PUNHO ============
    WRIST(
        description = "Wrist",
        proximalSegment = BodySegmentEnum.RIGHT_FOREARM,
        distalSegment = BodySegmentEnum.RIGHT_HAND,
        movements = setOf(
            MovementEnum.FLEXION,
            MovementEnum.EXTENSION,
            MovementEnum.ULNAR_ADDUCTION,
            MovementEnum.RADIAL_ADDUCTION
        )
    ),

    // ============ CARPOMETACARPAL DO POLEGAR ============
    CARPOMETACARPAL_THUMB(
        description = "Carpometacarpal thumb",
        proximalSegment = BodySegmentEnum.RIGHT_HAND,
        distalSegment = BodySegmentEnum.RIGHT_THUMB,
        movements = setOf(
            MovementEnum.FLEXION,
            MovementEnum.EXTENSION,
            MovementEnum.ABDUCTION,
            MovementEnum.ADDUCTION,
            MovementEnum.THUMB_INTERNAL_FLEXION,
            MovementEnum.THUMB_INTERNAL_EXTENSION
        )
    ),

    // ============ METACARPOFALANGEANA ============
    METACARPOPHALANGEAL(
        description = "Metacarpophalangeal",
        proximalSegment = BodySegmentEnum.RIGHT_HAND,
        distalSegment = BodySegmentEnum.RIGHT_INDEX, // Pode variar por dedo
        movements = setOf(
            MovementEnum.FLEXION,
            MovementEnum.EXTENSION,
            MovementEnum.ABDUCTION,
            MovementEnum.ADDUCTION
        )
    ),

    // ============ INTERFALANGEANA PROXIMAL ============
    PROXIMAL_INTERPHALANGEAL(
        description = "Proximal Interphalangeal",
        proximalSegment = null, // Definido dinamicamente por dedo
        distalSegment = null,
        movements = setOf(
            MovementEnum.FLEXION,
            MovementEnum.EXTENSION,
            MovementEnum.INTERNAL_EXTENSION_FINGERS
        )
    ),

    // ============ INTERFALANGEANA DISTAL ============
    DISTAL_INTERPHALANGEAL(
        description = "Distal Interphalangeal",
        proximalSegment = null,
        distalSegment = null,
        movements = setOf(
            MovementEnum.FLEXION,
            MovementEnum.EXTENSION
        )
    );

    /**
     * Verifica se este procedimento suporta um determinado movimento
     */
    fun supportsMovement(movement: MovementEnum): Boolean {
        return movements.contains(movement)
    }

    companion object {
        /**
         * Encontra procedimentos que suportam um movimento específico
         */
        fun findByMovement(movement: MovementEnum): List<ProcedureEnum> {
            return ProcedureEnum.entries.filter { it.movements.contains(movement) }
        }
    }
}