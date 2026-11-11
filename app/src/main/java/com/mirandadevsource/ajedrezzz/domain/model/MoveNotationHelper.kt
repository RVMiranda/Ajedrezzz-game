package com.mirandadevsource.ajedrezzz.domain.model

object MoveNotationHelper {

    fun formatMove(
        piece: Piece,
        from: Position,
        to: Position,
        isCapture: Boolean,
        isCheck: Boolean,
        isCheckmate: Boolean,
        promotionPiece: PieceType? = null
    ): String {
        // Enroque (Rey se mueve 2 casillas horizontalmente)
        if (piece.type == PieceType.KING && kotlin.math.abs(from.file - to.file) == 2) {
            val castlingStr = if (to.file > from.file) "0-0" else "0-0-0"
            return when {
                isCheckmate -> "$castlingStr#"
                isCheck -> "$castlingStr+"
                else -> castlingStr
            }
        }

        val piecePrefix = when (piece.type) {
            PieceType.PAWN -> if (isCapture) "${('a' + from.file)}" else ""
            PieceType.KNIGHT -> "C"
            PieceType.BISHOP -> "A"
            PieceType.ROOK -> "T"
            PieceType.QUEEN -> "D"
            PieceType.KING -> "R"
        }

        val captureSign = if (isCapture) "x" else ""
        val targetSquare = to.algebraic

        val promoSuffix = if (promotionPiece != null) {
            "=" + when (promotionPiece) {
                PieceType.QUEEN -> "D"
                PieceType.ROOK -> "T"
                PieceType.BISHOP -> "A"
                PieceType.KNIGHT -> "C"
                else -> "D"
            }
        } else ""

        val checkSuffix = when {
            isCheckmate -> "#"
            isCheck -> "+"
            else -> ""
        }

        return "$piecePrefix$captureSign$targetSquare$promoSuffix$checkSuffix"
    }
}
