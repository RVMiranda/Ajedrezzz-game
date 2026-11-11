package com.mirandadevsource.ajedrezzz.domain.model

sealed class MoveResult {
    data class Success(
        val from: Position,
        val to: Position,
        val pieceMoved: Piece,
        val capturedPiece: Piece?,
        val gameStatus: GameStatus
    ) : MoveResult()

    data class PromotionRequired(
        val from: Position,
        val to: Position
    ) : MoveResult()

    data class IllegalMove(val reason: String) : MoveResult()
}
