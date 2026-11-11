package com.mirandadevsource.ajedrezzz.ui.game.components

import androidx.compose.ui.graphics.Color

object ChessColors {
    // Colores clásicos descansados de tablero (estilo Lichess/Chess.com)
    val LightSquare = Color(0xFFEEEED2)
    val DarkSquare = Color(0xFF769656)

    // Resaltado de casilla seleccionada
    val SelectedSquare = Color(0xFFBACA44)

    // Resaltado de última jugada realizada
    val LastMoveHighlight = Color(0x66F5F682)

    // Indicador de casillas legales de movimiento pacífico
    val LegalMoveDot = Color(0x35000000)

    // Indicador de casilla legal de captura
    val LegalCaptureRing = Color(0x50000000)

    // Alerta de Rey en Jaque
    val CheckHighlight = Color(0xB3EB5757)
    val CheckBorder = Color(0xFFD32F2F)

    // Alerta pedagógica: Pieza bajo ataque / amenazada (Modo Tutor)
    val ThreatenedHighlight = Color(0x55FF9800)
    val ThreatenedBorder = Color(0xFFE65100)

    // Letras y números de coordenadas
    val CoordinateTextDark = Color(0xFF769656)
    val CoordinateTextLight = Color(0xFFEEEED2)
}
