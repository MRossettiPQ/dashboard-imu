package com.rot.gonimetry.enums

enum class BodyRegionEnum(val description: String) {
    // Membros Superiores
    SHOULDER("Ombro"),
    ARM("Braço"),
    ELBOW("Cotovelo"),
    FOREARM("Antebraço"),
    WRIST("Punho"),
    HAND("Mão"),

    // Tronco
    CERVICAL_SPINE("Coluna cervical"),
    THORACIC_SPINE("Coluna torácica"),
    LUMBAR_SPINE("Coluna lombar"),
    PELVIS("Pelve"),

    // Membros Inferiores
    HIP("Quadril"),
    THIGH("Coxa"),
    KNEE("Joelho"),
    LEG("Perna"),
    ANKLE("Tornozelo"),
    FOOT("Pé")
}