package com.rot.gonimetry.enums

import com.rot.session.enums.BodySegmentEnum

enum class ArticulationEnum(
    val bodySegment: BodySegmentEnum,
    val description: String,
    val isBilateral: Boolean, // Indica se tem lado (Direito/Esquerdo) ou se é Central
    val movements: Set<MovementEnum> = emptySet(),
) {
    SAMPLE(BodySegmentEnum.SPINE, "Coluna Cervical", false),
    // --- COLUNA (SPINE) ---
    CERVICAL_SPINE(BodySegmentEnum.SPINE, "Coluna Cervical", false),
    THORACIC_SPINE(BodySegmentEnum.SPINE, "Coluna Torácica", false),
    LUMBAR_SPINE(BodySegmentEnum.SPINE, "Coluna Lombar", false),

    // --- MEMBRO SUPERIOR (UPPER_LIMB) ---
    SHOULDER(BodySegmentEnum.UPPER_LIMB, "Ombro", true),
    ELBOW(BodySegmentEnum.UPPER_LIMB, "Cotovelo", true),
    WRIST(BodySegmentEnum.UPPER_LIMB, "Punho", true),

    // --- GONIOMETRIA ESPECIAL: MÃO E DEDOS (Section 4) ---
    // Polegar
    THUMB_CMC(BodySegmentEnum.UPPER_LIMB, "Carpometacarpiana do Polegar", true),
    THUMB_MCP(BodySegmentEnum.UPPER_LIMB, "Metacarpofalângica do Polegar", true),
    THUMB_IP(BodySegmentEnum.UPPER_LIMB, "Interfalângica do Polegar", true),

    // Dedos (II a V)
    FINGERS_MCP(BodySegmentEnum.UPPER_LIMB, "Metacarpofalângicas (Dedos II-V)", true),
    FINGERS_PIP(BodySegmentEnum.UPPER_LIMB, "Interfalângicas Proximais (Dedos II-V)", true),
    FINGERS_DIP(BodySegmentEnum.UPPER_LIMB, "Interfalângicas Distais (Dedos II-V)", true),

    // --- MEMBRO INFERIOR (LOWER_LIMB) ---
    HIP(BodySegmentEnum.LOWER_LIMB, "Quadril", true),
    KNEE(BodySegmentEnum.LOWER_LIMB, "Joelho", true),
    ANKLE(BodySegmentEnum.LOWER_LIMB, "Tornozelo", true),
    SUBTALAR(BodySegmentEnum.LOWER_LIMB, "Subtalar", true),

    // --- GONIOMETRIA ESPECIAL: PÉ E DEDOS (Section 4) ---
    TOES_MTP(BodySegmentEnum.LOWER_LIMB, "Metatarsofalângicas (Dedos do Pé)", true),
    TOES_IP(BodySegmentEnum.LOWER_LIMB, "Interfalângicas (Dedos do Pé)", true),
    HALLUX_MTP(BodySegmentEnum.LOWER_LIMB, "Metatarsofalângica do Hálux", true),
    HALLUX_IP(BodySegmentEnum.LOWER_LIMB, "Interfalângica do Hálux", true),

    // --- GONIOMETRIA ESPECIAL: CABEÇA (HEAD) ---
    // A ATM é uma unidade funcional, mas possui côndilos D e E.
    // Movimentos como abertura são centrais, desvios são laterais.
    TEMPOROMANDIBULAR(BodySegmentEnum.HEAD, "Articulação Temporomandibular (ATM)", false)
}