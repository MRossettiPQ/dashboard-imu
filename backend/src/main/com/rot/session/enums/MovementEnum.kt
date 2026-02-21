package com.rot.session.enums

enum class MovementEnum(val description: String) {
    // --- MOVIMENTOS GERAIS ---
    FLEXION("Flexão"),
    EXTENSION("Extensão"),
    ABDUCTION("Abdução"),
    ADDUCTION("Adução"),
    INTERNAL_ROTATION("Rotação Interna"),
    EXTERNAL_ROTATION("Rotação Externa"),

    // --- COLUNA (Específicos de direção) ---
    LATERAL_FLEXION_RIGHT("Flexão Lateral Direita"),
    LATERAL_FLEXION_LEFT("Flexão Lateral Esquerda"),
    ROTATION_RIGHT("Rotação Direita"),
    ROTATION_LEFT("Rotação Esquerda"),

    // --- PUNHO E MÃO ---
    RADIAL_DEVIATION("Desvio Radial"),
    ULNAR_DEVIATION("Desvio Ulnar"),

    // --- GONIOMETRIA ESPECIAL: POLEGAR E DEDOS (Section 4) ---
    OPPOSITION("Oposição"), // Específico do Polegar (Cap. 4)
    PALMAR_ABDUCTION("Abdução Palmar"), // Polegar
    RADIAL_ABDUCTION("Abdução Radial"), // Polegar

    // --- PÉ E TORNOZELO ---
    DORSIFLEXION("Dorsiflexão"),
    PLANTAR_FLEXION("Flexão Plantar"),
    INVERSION("Inversão"),
    EVERSION("Eversão"),

    // --- GONIOMETRIA ESPECIAL: ATM (Section 4) ---
    MANDIBULAR_DEPRESSION("Depressão (Abertura da Boca)"),
    MANDIBULAR_PROTRUSION("Protrusão"),
    MANDIBULAR_RETRUSION("Retração"), // Ou Retrusão
    MANDIBULAR_LATERAL_DEVIATION_RIGHT("Desvio Lateral Direito"),
    MANDIBULAR_LATERAL_DEVIATION_LEFT("Desvio Lateral Esquerdo"),

    // --- OUTROS ---
    ELEVATION("Elevação"), // Ex: Escápula
    DEPRESSION("Depressão"), // Ex: Escápula
    PROTRACTION("Protração"),
    RETRACTION("Retração")
}