package com.mirandadevsource.ajedrezzz.domain.model

/**
 * Representa el estado visual y pedagógico de una casilla individual del tablero.
 */
data class SquareUiModel(
    val position: Position,
    val piece: Piece? = null,
    val isSelected: Boolean = false,
    val isLegalDestination: Boolean = false,
    val isCaptureDestination: Boolean = false,
    val isSafeDestination: Boolean = false,
    val isDangerousDestination: Boolean = false,
    val isHintSource: Boolean = false,
    val isHintDestination: Boolean = false,
    val isCheck: Boolean = false,
    val isThreatened: Boolean = false
) {
    val isLightSquare: Boolean
        get() = (position.file + position.rank) % 2 != 0
}
