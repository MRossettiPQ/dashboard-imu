package com.rot.session.enums

/**
 * Tipos de movimento com informações sobre o plano anatômico
 */
enum class MovementEnum(
    val description: String,
    val plane: AnatomicalPlaneEnum,
    val positiveDirection: Boolean = true
) {
    SIMPLE("Simple", AnatomicalPlaneEnum.SAGITTAL),

    // Plano Sagital (Flexão/Extensão)
    FLEXION("Flexion", AnatomicalPlaneEnum.SAGITTAL, true),
    EXTENSION("Extension", AnatomicalPlaneEnum.SAGITTAL, false),

    // Plano Frontal (Abdução/Adução)
    ADDUCTION("Adduction", AnatomicalPlaneEnum.FRONTAL, true),
    ABDUCTION("Abduction", AnatomicalPlaneEnum.FRONTAL, false),

    // Plano Transversal (Rotações)
    INTERNAL_ROTATION("Internal Rotation", AnatomicalPlaneEnum.TRANSVERSE, true),
    EXTERNAL_ROTATION("External Rotation", AnatomicalPlaneEnum.TRANSVERSE, false),

    // Antebraço
    PRONATION("Pronation", AnatomicalPlaneEnum.TRANSVERSE, true),
    SUPINATION("Supination", AnatomicalPlaneEnum.TRANSVERSE, false),

    // Polegar específico
    THUMB_INTERNAL_FLEXION("Thumb internal flexion", AnatomicalPlaneEnum.SAGITTAL, true),
    THUMB_INTERNAL_EXTENSION("Thumb internal extension", AnatomicalPlaneEnum.SAGITTAL, false),

    // Dedos
    INTERNAL_EXTENSION_FINGERS("Internal extensions fingers", AnatomicalPlaneEnum.SAGITTAL, false),

    // Punho (desvios)
    ULNAR_ADDUCTION("Ulnar adduction", AnatomicalPlaneEnum.FRONTAL, true),
    RADIAL_ADDUCTION("Radial adduction", AnatomicalPlaneEnum.FRONTAL, false);

    /**
     * Retorna o movimento oposto (ex: FLEXION ↔ EXTENSION)
     */
    fun opposite(): MovementEnum? {
        return when (this) {
            FLEXION -> EXTENSION
            EXTENSION -> FLEXION
            ADDUCTION -> ABDUCTION
            ABDUCTION -> ADDUCTION
            INTERNAL_ROTATION -> EXTERNAL_ROTATION
            EXTERNAL_ROTATION -> INTERNAL_ROTATION
            PRONATION -> SUPINATION
            SUPINATION -> PRONATION
            THUMB_INTERNAL_FLEXION -> THUMB_INTERNAL_EXTENSION
            THUMB_INTERNAL_EXTENSION -> THUMB_INTERNAL_FLEXION
            ULNAR_ADDUCTION -> RADIAL_ADDUCTION
            RADIAL_ADDUCTION -> ULNAR_ADDUCTION
            else -> null
        }
    }
}

/**
 * Planos anatômicos de movimento
 */
enum class AnatomicalPlaneEnum(val description: String) {
    SAGITTAL("Sagittal Plane"),      // Flexão/Extensão - pitch
    FRONTAL("Frontal Plane"),         // Abdução/Adução - roll
    TRANSVERSE("Transverse Plane")    // Rotação - yaw
}