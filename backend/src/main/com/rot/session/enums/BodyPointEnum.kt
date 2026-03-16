package com.rot.gonimetry.enums

/**
 * Pontos anatômicos onde sensores podem ser posicionados no corpo.
 *
 * Cada ponto representa um local físico no corpo humano onde um sensor IMU
 * pode ser fixado para capturar dados de orientação. Os pontos foram definidos
 * com base no manual de goniometria, considerando:
 *
 * - Os segmentos ósseos adjacentes a cada articulação avaliada
 * - Os pontos de referência anatômicos usados para posicionamento do goniômetro
 * - A necessidade de compartilhamento de sensores entre articulações vizinhas
 *   (ex: sensor na coxa participa da medição do quadril E do joelho)
 *
 * Nomenclatura: Os nomes seguem o padrão do segmento corporal ou referência
 * anatômica específica. O lado (direito/esquerdo) NÃO faz parte do enum,
 * sendo definido no campo `side` da entidade [SessionNode].
 */
enum class BodyPointEnum(
    val description: String,
    val region: BodyRegionEnum
) {

    // =========================================================================
    // MEMBROS SUPERIORES
    // =========================================================================

    // --- Ombro / Escápula ---
    ACROMION(
        description = "Acrômio - referência para eixo do ombro",
        region = BodyRegionEnum.SHOULDER
    ),
    LATERAL_TRUNK(
        description = "Linha axilar média do tronco - braço fixo para flexão/extensão do ombro",
        region = BodyRegionEnum.SHOULDER
    ),
    SCAPULA(
        description = "Escápula - referência para movimentos escapulares",
        region = BodyRegionEnum.SHOULDER
    ),

    // --- Braço (Úmero) ---
    LATERAL_HUMERUS(
        description = "Superfície lateral do corpo do úmero - braço móvel para ombro, fixo para cotovelo",
        region = BodyRegionEnum.ARM
    ),

    // --- Cotovelo ---
    LATERAL_EPICONDYLE(
        description = "Epicôndilo lateral do úmero - referência distal do úmero",
        region = BodyRegionEnum.ELBOW
    ),
    OLECRANON(
        description = "Olécrano - eixo para rotação medial/lateral do ombro",
        region = BodyRegionEnum.ELBOW
    ),

    // --- Antebraço (Rádio/Ulna) ---
    POSTERIOR_FOREARM(
        description = "Região posterior do antebraço - braço móvel para rotações do ombro",
        region = BodyRegionEnum.FOREARM
    ),
    STYLOID_PROCESS_RADIUS(
        description = "Processo estiloide do rádio - referência distal do antebraço",
        region = BodyRegionEnum.FOREARM
    ),
    STYLOID_PROCESS_ULNA(
        description = "Processo estiloide da ulna - referência para pronação/supinação",
        region = BodyRegionEnum.FOREARM
    ),

    // --- Mão ---
    DORSAL_HAND(
        description = "Dorso da mão - superfície dorsal do terceiro metacarpal",
        region = BodyRegionEnum.HAND
    ),
    THIRD_METACARPAL(
        description = "Terceiro metacarpal - referência para flexão/extensão do punho",
        region = BodyRegionEnum.HAND
    ),
    FIRST_METACARPAL(
        description = "Primeiro metacarpal - referência para movimentos do polegar",
        region = BodyRegionEnum.HAND
    ),

    // =========================================================================
    // TRONCO / COLUNA VERTEBRAL
    // =========================================================================

    // --- Coluna Cervical ---
    OCCIPITAL_PROTUBERANCE(
        description = "Protuberância occipital externa - referência para flexão lateral cervical",
        region = BodyRegionEnum.CERVICAL_SPINE
    ),
    VERTEX(
        description = "Centro da cabeça (sutura sagital) - eixo para rotação cervical e lombar",
        region = BodyRegionEnum.CERVICAL_SPINE
    ),
    EAR_LOBE(
        description = "Lóbulo da orelha - braço móvel para flexão/extensão cervical",
        region = BodyRegionEnum.CERVICAL_SPINE
    ),
    C7_SPINOUS_PROCESS(
        description = "Processo espinhoso da 7ª vértebra cervical - transição cervical/torácica",
        region = BodyRegionEnum.CERVICAL_SPINE
    ),

    // --- Coluna Lombar ---
    ILIAC_CREST(
        description = "Crista ilíaca - referência para flexão/extensão lombar",
        region = BodyRegionEnum.LUMBAR_SPINE
    ),
    ASIS(
        description = "Espinha ilíaca ântero-superior (EIAS) - eixo para movimentos lombares",
        region = BodyRegionEnum.LUMBAR_SPINE
    ),
    PSIS(
        description = "Espinha ilíaca póstero-superior (EIPS) - referência para flexão lateral lombar",
        region = BodyRegionEnum.LUMBAR_SPINE
    ),
    SACRAL_CREST(
        description = "Crista sacral mediana - eixo para flexão lateral lombar",
        region = BodyRegionEnum.LUMBAR_SPINE
    ),

    // =========================================================================
    // MEMBROS INFERIORES
    // =========================================================================

    // --- Quadril ---
    GREATER_TROCHANTER(
        description = "Trocanter maior do fêmur - eixo para flexão/extensão/abdução/adução do quadril",
        region = BodyRegionEnum.HIP
    ),

    // --- Coxa (Fêmur) ---
    LATERAL_FEMUR(
        description = "Superfície lateral da coxa (diáfise do fêmur) - segmento femoral",
        region = BodyRegionEnum.THIGH
    ),
    ANTERIOR_FEMUR(
        description = "Região anterior da coxa - referência para abdução/adução do quadril",
        region = BodyRegionEnum.THIGH
    ),

    // --- Joelho ---
    LATERAL_FEMORAL_CONDYLE(
        description = "Côndilo lateral do fêmur - referência distal do fêmur / eixo do joelho",
        region = BodyRegionEnum.KNEE
    ),
    PATELLA(
        description = "Face anterior da patela - eixo para rotações do quadril e ângulo Q",
        region = BodyRegionEnum.KNEE
    ),
    TIBIAL_TUBEROSITY(
        description = "Tuberosidade da tíbia - referência para rotação medial/lateral do quadril",
        region = BodyRegionEnum.KNEE
    ),

    // --- Perna (Tíbia/Fíbula) ---
    LATERAL_FIBULA(
        description = "Face lateral da fíbula - braço móvel para flexão do joelho, fixo para tornozelo",
        region = BodyRegionEnum.LEG
    ),
    ANTERIOR_TIBIA(
        description = "Margem anterior da tíbia - referência para inversão/eversão do pé",
        region = BodyRegionEnum.LEG
    ),

    // --- Tornozelo ---
    LATERAL_MALLEOLUS(
        description = "Maléolo lateral - eixo para movimentos do tornozelo",
        region = BodyRegionEnum.ANKLE
    ),
    MEDIAL_MALLEOLUS(
        description = "Maléolo medial - referência para valgo de joelhos (fita métrica)",
        region = BodyRegionEnum.ANKLE
    ),

    // --- Pé ---
    FIFTH_METATARSAL(
        description = "Superfície lateral do 5º metatarsal - braço móvel para flexão plantar",
        region = BodyRegionEnum.FOOT
    ),
    SECOND_METATARSAL(
        description = "Superfície dorsal do 2º metatarsal - referência para inversão do pé",
        region = BodyRegionEnum.FOOT
    ),
    CALCANEUS(
        description = "Calcâneo - referência para varo/valgo de retropé",
        region = BodyRegionEnum.FOOT
    ),
    DORSAL_FOOT(
        description = "Dorso do pé - superfície dorsal para referência geral do pé",
        region = BodyRegionEnum.FOOT
    );

    companion object {
        fun findByRegion(region: BodyRegionEnum): List<BodyPointEnum> {
            return entries.filter { it.region == region }
        }
    }
}