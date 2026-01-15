package com.rot.session.enums

/**
 * Segmentos corporais onde sensores podem ser posicionados
 * Cada sensor em um Movement deve ser associado a um segmento
 */
enum class BodySegmentEnum(
    val description: String,
    val side: BodySideEnum? = null
) {
    // Tronco
    HEAD("Head"),
    NECK("Neck"),
    THORAX("Thorax"),
    PELVIS("Pelvis"),

    // Membro Superior Direito
    RIGHT_UPPER_ARM("Right Upper Arm", BodySideEnum.RIGHT),
    RIGHT_FOREARM("Right Forearm", BodySideEnum.RIGHT),
    RIGHT_HAND("Right Hand", BodySideEnum.RIGHT),
    RIGHT_THUMB("Right Thumb", BodySideEnum.RIGHT),
    RIGHT_INDEX("Right Index Finger", BodySideEnum.RIGHT),
    RIGHT_MIDDLE("Right Middle Finger", BodySideEnum.RIGHT),
    RIGHT_RING("Right Ring Finger", BodySideEnum.RIGHT),
    RIGHT_PINKY("Right Pinky Finger", BodySideEnum.RIGHT),

    // Membro Superior Esquerdo
    LEFT_UPPER_ARM("Left Upper Arm", BodySideEnum.LEFT),
    LEFT_FOREARM("Left Forearm", BodySideEnum.LEFT),
    LEFT_HAND("Left Hand", BodySideEnum.LEFT),
    LEFT_THUMB("Left Thumb", BodySideEnum.LEFT),
    LEFT_INDEX("Left Index Finger", BodySideEnum.LEFT),
    LEFT_MIDDLE("Left Middle Finger", BodySideEnum.LEFT),
    LEFT_RING("Left Ring Finger", BodySideEnum.LEFT),
    LEFT_PINKY("Left Pinky Finger", BodySideEnum.LEFT),

    // Membro Inferior Direito
    RIGHT_THIGH("Right Thigh", BodySideEnum.RIGHT),
    RIGHT_SHANK("Right Shank", BodySideEnum.RIGHT),
    RIGHT_FOOT("Right Foot", BodySideEnum.RIGHT),

    // Membro Inferior Esquerdo
    LEFT_THIGH("Left Thigh", BodySideEnum.LEFT),
    LEFT_SHANK("Left Shank", BodySideEnum.LEFT),
    LEFT_FOOT("Left Foot", BodySideEnum.LEFT);
}

enum class BodySideEnum {
    LEFT, RIGHT
}