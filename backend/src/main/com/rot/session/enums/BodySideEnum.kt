package com.rot.session.enums

enum class BodySideEnum(
    val description: String,
    val comments: String,
) {
    RIGHT(
        description = "Direita",
        comments = "Membros (Ombro direito, Joelho direito)"
    ),

    LEFT(
        description = "Esquerda",
        comments = "Membros (Ombro esquerdo, Joelho esquerdo)"
    ),
    CENTRAL(
        description = "Centro",
        comments = "Coluna (Cervical, Lombar, Torácica)"
    );
}