package com.rot.session.enums

enum class BodyRegionEnum(val description: String, val validSegments: List<BodySegmentationEnum> = emptyList()) {
    KNEE_LEFT("Perna esquerda", listOf(BodySegmentationEnum.LEFT_THIGH, BodySegmentationEnum.LEFT_SHIN)),
    KNEE_RIGHT("Perna direita", listOf(BodySegmentationEnum.RIGHT_THIGH, BodySegmentationEnum.RIGHT_SHIN)),
    ANKLE_LEFT("Tornozelo esquerdo", listOf(BodySegmentationEnum.LEFT_SHIN, BodySegmentationEnum.LEFT_FOOT)),
    ANKLE_RIGHT("Tornozelo direito", listOf(BodySegmentationEnum.RIGHT_SHIN, BodySegmentationEnum.RIGHT_FOOT)),
    HIP_LEFT("Quadril esquerda", listOf(BodySegmentationEnum.HIP, BodySegmentationEnum.LEFT_THIGH)),
    HIP_RIGHT("Quadril direita", listOf(BodySegmentationEnum.HIP, BodySegmentationEnum.RIGHT_THIGH)),
    ELBOW_LEFT("Cotovelo esquerdo", listOf(BodySegmentationEnum.LEFT_ARM, BodySegmentationEnum.LEFT_FOREARM)),
    ELBOW_RIGHT("Cotovelo direito", listOf(BodySegmentationEnum.RIGHT_ARM, BodySegmentationEnum.RIGHT_FOREARM)),
    WRIST_LEFT("Punho esquerdo", listOf(BodySegmentationEnum.LEFT_FOREARM, BodySegmentationEnum.LEFT_HAND)),
    WRIST_RIGHT("Punho direito", listOf(BodySegmentationEnum.RIGHT_FOREARM, BodySegmentationEnum.RIGHT_HAND)),
}