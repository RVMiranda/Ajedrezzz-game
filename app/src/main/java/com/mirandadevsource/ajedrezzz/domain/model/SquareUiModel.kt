package com.mirandadevsource.ajedrezzz.domain.model

/**
 * Representa el estado visual de una casilla individual del tablero.
 */
data class SquareUiModel(
    val position: Position,
    val piece: Piece? = null,
    val isSelected: Boolean = false,
    val isLegalDestination: Boolean = false,
    val isCaptureDestination: Boolean = false,
    val isCheck: Boolean = false,
    val isThreatened: Boolean = false
) {
    val isLightSquare: Boolean
        get() = (position.file + position.rank) % 2 != 0
}
