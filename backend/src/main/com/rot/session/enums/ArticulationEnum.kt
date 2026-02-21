package com.rot.session.enums

enum class ArticulationEnum(
    val description: String,
    val bodySegment: BodySegmentEnum,
    val movements: Set<MovementEnum> = emptySet(),
    val isBilateral: Boolean // Indica se tem lado (Direito/Esquerdo) ou se é Central
) {
    // --- COLUNA (SPINE) ---
    CERVICAL_SPINE(SPINE, "Coluna Cervical", false),
    THORACIC_SPINE(SPINE, "Coluna Torácica", false),
    LUMBAR_SPINE(SPINE, "Coluna Lombar", false),

    // --- MEMBRO SUPERIOR (UPPER_LIMB) ---
    SHOULDER(UPPER_LIMB, "Ombro", true),
    ELBOW(UPPER_LIMB, "Cotovelo", true),
    WRIST(UPPER_LIMB, "Punho", true),

    // --- GONIOMETRIA ESPECIAL: MÃO E DEDOS (Section 4) ---
    // Polegar
    THUMB_CMC(UPPER_LIMB, "Carpometacarpiana do Polegar", true),
    THUMB_MCP(UPPER_LIMB, "Metacarpofalângica do Polegar", true),
    THUMB_IP(UPPER_LIMB, "Interfalângica do Polegar", true),

    // Dedos (II a V)
    FINGERS_MCP(UPPER_LIMB, "Metacarpofalângicas (Dedos II-V)", true),
    FINGERS_PIP(UPPER_LIMB, "Interfalângicas Proximais (Dedos II-V)", true),
    FINGERS_DIP(UPPER_LIMB, "Interfalângicas Distais (Dedos II-V)", true),

    // --- MEMBRO INFERIOR (LOWER_LIMB) ---
    HIP(LOWER_LIMB, "Quadril", true),
    KNEE(LOWER_LIMB, "Joelho", true),
    ANKLE(LOWER_LIMB, "Tornozelo", true),
    SUBTALAR(LOWER_LIMB, "Subtalar", true),

    // --- GONIOMETRIA ESPECIAL: PÉ E DEDOS (Section 4) ---
    TOES_MTP(LOWER_LIMB, "Metatarsofalângicas (Dedos do Pé)", true),
    TOES_IP(LOWER_LIMB, "Interfalângicas (Dedos do Pé)", true),
    HALLUX_MTP(LOWER_LIMB, "Metatarsofalângica do Hálux", true),
    HALLUX_IP(LOWER_LIMB, "Interfalângica do Hálux", true),

    // --- GONIOMETRIA ESPECIAL: CABEÇA (HEAD) ---
    // A ATM é uma unidade funcional, mas possui côndilos D e E.
    // Movimentos como abertura são centrais, desvios são laterais.
    TEMPOROMANDIBULAR(HEAD, "Articulação Temporomandibular (ATM)", false)
}