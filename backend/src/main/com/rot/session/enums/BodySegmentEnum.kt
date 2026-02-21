package com.rot.session.enums

enum class BodySegmentEnum(val description: String) {
    HEAD("Cabeça"),              // Para ATM (Goniometria Especial)
    SPINE("Coluna Vertebral"),   // Para Cervical, Torácica e Lombar
    UPPER_LIMB("Membro Superior"), // Inclui Mão e Dedos (Goniometria Especial)
    LOWER_LIMB("Membro Inferior")  // Inclui Pé e Dedos do Pé (Goniometria Especial)
}