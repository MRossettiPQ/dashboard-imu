package com.rot.gonimetry.enums

enum class ArticulationEnum(
    val description: String,
) {
    SAMPLE("SAMPLE"),                        // Flexão
    FLEXION("FLEXION"),                        // Flexão
    EXTENSION("EXTENSION"),                      // Extensão
    ABDUCTION("ABDUCTION"),                      // Abdução
    ADDUCTION("ADDUCTION"),                      // Adução
    MEDIAL_ROTATION("MEDIAL_ROTATION"),                // Rotação medial
    LATERAL_ROTATION("LATERAL_ROTATION"),               // Rotação lateral
    PRONATION("PRONATION"),                      // Pronação
    SUPINATION("SUPINATION"),                     // Supinação
    DORSIFLEXION("DORSIFLEXION"),                   // Flexão dorsal
    PLANTARFLEXION("PLANTARFLEXION"),                 // Flexão plantar
    INVERSION("INVERSION"),                      // Inversão (adução do pé)
    EVERSION("EVERSION"),                       // Eversão (abdução do pé)
    LATERAL_FLEXION("LATERAL_FLEXION"),                // Flexão lateral (coluna)
    ROTATION("ROTATION"),                       // Rotação (coluna)
    FLEXION_FIRST_TOE("FLEXION_FIRST_TOE"),              // Flexão do 1° dedo
    FLEXION_SECOND_TO_FIFTH_TOE("FLEXION_SECOND_TO_FIFTH_TOE"),    // Flexão do 2° ao 5° dedo
    EXTENSION_FIRST_TOE("EXTENSION_FIRST_TOE"),            // Extensão do 1° dedo
    EXTENSION_SECOND_TO_FIFTH_TOE("EXTENSION_SECOND_TO_FIFTH_TOE"),  // Extensão do 2° ao 5° dedo
    FLEXION_IP_SECOND_TO_FIFTH_TOE("FLEXION_IP_SECOND_TO_FIFTH_TOE"), // Flexão interfalângica proximal 2°-5° dedo
    FLEXION_ID_SECOND_TO_FIFTH_TOE("FLEXION_ID_SECOND_TO_FIFTH_TOE")  // Flexão interfalângica distal 2°-5° dedo
}