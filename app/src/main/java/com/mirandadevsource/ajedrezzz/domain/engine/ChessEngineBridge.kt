package com.mirandadevsource.ajedrezzz.domain.engine

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.mirandadevsource.ajedrezzz.domain.model.GameStatus
import com.mirandadevsource.ajedrezzz.domain.model.MoveResult
import com.mirandadevsource.ajedrezzz.domain.model.Piece
import com.mirandadevsource.ajedrezzz.domain.model.PieceColor
import com.mirandadevsource.ajedrezzz.domain.model.PieceType
import com.mirandadevsource.ajedrezzz.domain.model.Position
import com.mirandadevsource.ajedrezzz.domain.model.SquareUiModel
import com.github.bhlangonijr.chesslib.Piece as LibPiece
import com.github.bhlangonijr.chesslib.PieceType as LibPieceType

class ChessEngineBridge(private val board: Board = Board()) {

    val sideToMove: PieceColor
        get() = if (board.sideToMove == Side.WHITE) PieceColor.WHITE else PieceColor.BLACK

    val canUndo: Boolean
        get() = board.backup.isNotEmpty()

    fun reset() {
        board.loadFromFen(Board().fen)
    }

    fun loadFen(fen: String) {
        board.loadFromFen(fen)
    }

    val currentFen: String
        get() = board.fen

    fun getPieceAt(position: Position): Piece? {
        val square = position.toSquare()
        val libPiece = board.getPiece(square)
        return libPiece.toDomainPiece()
    }

    fun getLegalMovesFrom(position: Position): List<Position> {
        val fromSquare = position.toSquare()
        return board.legalMoves()
            .filter { it.from == fromSquare }
            .map { it.to.toPosition() }
            .distinct()
    }

    fun isPromotion(from: Position, to: Position): Boolean {
        val piece = getPieceAt(from) ?: return false
        if (piece.type != PieceType.PAWN) return false

        return (piece.color == PieceColor.WHITE && to.rank == 7) ||
                (piece.color == PieceColor.BLACK && to.rank == 0)
    }

    fun makeMove(
        from: Position,
        to: Position,
        promotionPieceType: PieceType = PieceType.QUEEN
    ): MoveResult {
        val fromSquare = from.toSquare()
        val toSquare = to.toSquare()
        val pieceToMove = getPieceAt(from)
            ?: return MoveResult.IllegalMove("No hay ninguna pieza en la casilla seleccionada")

        if (pieceToMove.color != sideToMove) {
            return MoveResult.IllegalMove("No es el turno de este jugador")
        }

        val legalMoves = board.legalMoves().filter { it.from == fromSquare && it.to == toSquare }
        if (legalMoves.isEmpty()) {
            return MoveResult.IllegalMove("Movimiento no permitido según las reglas")
        }

        // Si es una promoción de peón y no se especificó un tipo explícito cuando hay múltiples opciones
        val isPromo = isPromotion(from, to)
        val selectedMove = if (isPromo) {
            val promoLibPiece = toLibPromotionPiece(pieceToMove.color, promotionPieceType)
            legalMoves.firstOrNull { it.promotion == promoLibPiece }
                ?: legalMoves.first()
        } else {
            legalMoves.first()
        }

        val capturedPiece = getPieceAt(to)
        val moveExecuted = board.doMove(selectedMove)
        if (!moveExecuted) {
            return MoveResult.IllegalMove("Error al ejecutar el movimiento en el motor")
        }

        return MoveResult.Success(
            from = from,
            to = to,
            pieceMoved = pieceToMove,
            capturedPiece = capturedPiece,
            gameStatus = getGameStatus()
        )
    }

    fun undoMove(): Boolean {
        if (!canUndo) return false
        board.undoMove()
        return true
    }

    fun isKingInCheck(): Boolean {
        return board.isKingAttacked
    }

    fun getKingPosition(color: PieceColor): Position {
        val side = if (color == PieceColor.WHITE) Side.WHITE else Side.BLACK
        val square = board.getKingSquare(side)
        return square.toPosition()
    }

    fun getGameStatus(): GameStatus {
        return when {
            board.isMated -> GameStatus.CHECKMATE
            board.isStaleMate -> GameStatus.STALEMATE
            board.isInsufficientMaterial -> GameStatus.DRAW_INSUFFICIENT_MATERIAL
            board.isRepetition -> GameStatus.DRAW_REPETITION
            board.isDraw -> GameStatus.DRAW_50_MOVES
            board.isKingAttacked -> GameStatus.CHECK
            else -> GameStatus.IN_PROGRESS
        }
    }

    /**
     * Identifica casillas que contienen piezas del color actual que están amenazadas
     * por el oponente (soporte pedagógico para detección de blunders y piezas colgadas).
     */
    fun getThreatenedPieces(color: PieceColor): Set<Position> {
        val opponentSide = if (color == PieceColor.WHITE) Side.BLACK else Side.WHITE
        val threatened = mutableSetOf<Position>()

        // Recorremos todas las piezas del color consultado
        for (file in 0..7) {
            for (rank in 0..7) {
                val pos = Position(file, rank)
                val piece = getPieceAt(pos)
                if (piece != null && piece.color == color) {
                    val square = pos.toSquare()
                    if (board.squareAttackedBy(square, opponentSide) != 0L) {
                        threatened.add(pos)
                    }
                }
            }
        }
        return threatened
    }

    fun getAllSquares(
        selectedPosition: Position? = null,
        showThreats: Boolean = false
    ): List<SquareUiModel> {
        val legalMoves = selectedPosition?.let { getLegalMovesFrom(it).toSet() } ?: emptySet()
        val kingInCheckPos = if (isKingInCheck()) getKingPosition(sideToMove) else null
        val threatenedPieces = if (showThreats) getThreatenedPieces(sideToMove) else emptySet()

        val squares = ArrayList<SquareUiModel>(64)
        for (rank in 0..7) {
            for (file in 0..7) {
                val pos = Position(file, rank)
                val piece = getPieceAt(pos)
                val isSelected = selectedPosition == pos
                val isLegal = legalMoves.contains(pos)
                val isCapture = isLegal && (piece != null && piece.color != sideToMove)
                val isCheck = kingInCheckPos == pos
                val isThreatened = threatenedPieces.contains(pos)

                squares.add(
                    SquareUiModel(
                        position = pos,
                        piece = piece,
                        isSelected = isSelected,
                        isLegalDestination = isLegal && !isCapture,
                        isCaptureDestination = isCapture,
                        isCheck = isCheck,
                        isThreatened = isThreatened
                    )
                )
            }
        }
        return squares
    }

    companion object {
        fun Position.toSquare(): Square {
            val fileChar = 'A' + file
            val rankChar = '1' + rank
            return Square.valueOf("$fileChar$rankChar")
        }

        fun Square.toPosition(): Position {
            val nameStr = this.name
            val file = nameStr[0] - 'A'
            val rank = nameStr[1] - '1'
            return Position(file, rank)
        }

        fun LibPiece.toDomainPiece(): Piece? {
            if (this == LibPiece.NONE) return null
            val color = if (this.pieceSide == Side.WHITE) PieceColor.WHITE else PieceColor.BLACK
            val type = when (this.pieceType) {
                LibPieceType.PAWN -> PieceType.PAWN
                LibPieceType.KNIGHT -> PieceType.KNIGHT
                LibPieceType.BISHOP -> PieceType.BISHOP
                LibPieceType.ROOK -> PieceType.ROOK
                LibPieceType.QUEEN -> PieceType.QUEEN
                LibPieceType.KING -> PieceType.KING
                else -> return null
            }
            return Piece(type, color)
        }

        fun toLibPromotionPiece(color: PieceColor, type: PieceType): LibPiece {
            return when (color) {
                PieceColor.WHITE -> when (type) {
                    PieceType.QUEEN -> LibPiece.WHITE_QUEEN
                    PieceType.ROOK -> LibPiece.WHITE_ROOK
                    PieceType.BISHOP -> LibPiece.WHITE_BISHOP
                    PieceType.KNIGHT -> LibPiece.WHITE_KNIGHT
                    else -> LibPiece.WHITE_QUEEN
                }
                PieceColor.BLACK -> when (type) {
                    PieceType.QUEEN -> LibPiece.BLACK_QUEEN
                    PieceType.ROOK -> LibPiece.BLACK_ROOK
                    PieceType.BISHOP -> LibPiece.BLACK_BISHOP
                    PieceType.KNIGHT -> LibPiece.BLACK_KNIGHT
                    else -> LibPiece.BLACK_QUEEN
                }
            }
        }
    }
}
