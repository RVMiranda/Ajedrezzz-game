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

    // Indicador estándar de casillas legales de movimiento pacífico (Tutor apagado)
    val LegalMoveDot = Color(0x35000000)

    // Indicador estándar de casilla legal de captura (Tutor apagado)
    val LegalCaptureRing = Color(0x50000000)

    // Alerta de Rey en Jaque
    val CheckHighlight = Color(0xB3EB5757)
    val CheckBorder = Color(0xFFD32F2F)

    // Alerta pedagógica: Pieza bajo ataque / amenazada (Modo Tutor)
    val ThreatenedHighlight = Color(0x55FF9800)
    val ThreatenedBorder = Color(0xFFE65100)

    // Modo Tutor Avanzado: Movimientos seguros sugeridos (Verde)
    val SafeMoveDot = Color(0xDD2E7D32)
    val SafeCaptureRing = Color(0xEE2E7D32)

    // Modo Tutor Avanzado: Advertencia de casilla peligrosa/vigilada por el rival (Ámbar/Naranja)
    val BlunderWarningDot = Color(0xFFFF6D00)
    val BlunderWarningRing = Color(0xFFFF3D00)

    // Modo Tutor Avanzado: Sugerencia de jugada óptima (Pista)
    val HintHighlight = Color(0x5500E676)
    val HintBorder = Color(0xFF00C853)

    // Letras y números de coordenadas
    val CoordinateTextDark = Color(0xFF769656)
    val CoordinateTextLight = Color(0xFFEEEED2)
}
