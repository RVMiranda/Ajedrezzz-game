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
     * Calcula la mejor jugada para el bot según la dificultad actual.
     */
    fun findBestBotMove(difficulty: com.mirandadevsource.ajedrezzz.domain.ai.BotDifficulty): Pair<Position, Position>? {
        val bot = com.mirandadevsource.ajedrezzz.domain.ai.ChessBotEngine()
        return bot.findBestMove(board, difficulty)
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

    /**
     * Calcula la ventaja de material para ambos bandos.
     * @return Pair(ventajaBlancas, ventajaNegras).
     * Por ejemplo, si las blancas tienen 3 puntos más, retorna Pair(3, 0).
     */
    fun getMaterialAdvantage(): Pair<Int, Int> {
        var whiteMaterial = 0
        var blackMaterial = 0
        for (file in 0..7) {
            for (rank in 0..7) {
                val piece = getPieceAt(Position(file, rank)) ?: continue
                if (piece.color == PieceColor.WHITE) {
                    whiteMaterial += piece.type.materialValue
                } else {
                    blackMaterial += piece.type.materialValue
                }
            }
        }
        val diff = whiteMaterial - blackMaterial
        return when {
            diff > 0 -> Pair(diff, 0)
            diff < 0 -> Pair(0, -diff)
            else -> Pair(0, 0)
        }
    }

    fun isSquareAttackedByOpponent(position: Position, friendlyColor: PieceColor): Boolean {
        val opponentSide = if (friendlyColor == PieceColor.WHITE) Side.BLACK else Side.WHITE
        return board.squareAttackedBy(position.toSquare(), opponentSide) != 0L
    }

    fun getAllSquares(
        selectedPosition: Position? = null,
        showThreats: Boolean = false,
        hintMove: Pair<Position, Position>? = null
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

                val isDangerous = if (showThreats && isLegal) {
                    isSquareAttackedByOpponent(pos, sideToMove)
                } else false

                val isSafe = if (showThreats && isLegal) {
                    !isDangerous
                } else false

                val isHintSource = hintMove != null && hintMove.first == pos
                val isHintDestination = hintMove != null && hintMove.second == pos

                squares.add(
                    SquareUiModel(
                        position = pos,
                        piece = piece,
                        isSelected = isSelected,
                        isLegalDestination = isLegal && !isCapture,
                        isCaptureDestination = isCapture,
                        isSafeDestination = isSafe,
                        isDangerousDestination = isDangerous,
                        isHintSource = isHintSource,
                        isHintDestination = isHintDestination,
                        isCheck = isCheck,
                        isThreatened = isThreatened
                    )
                )
            }
        }
        return squares
    }

    /**
     * Sugiere una jugada táctica del motor para el bando en turno (Pistas del Modo Tutor).
     */
    fun findHintMove(): Pair<Position, Position>? {
        val bot = com.mirandadevsource.ajedrezzz.domain.ai.ChessBotEngine()
        return bot.findBestMove(board, com.mirandadevsource.ajedrezzz.domain.ai.BotDifficulty.MEDIUM)
    }

    /**
     * Genera una explicación pedagógica en español para la pista sugerida.
     */
    fun generateHintExplanation(from: Position, to: Position): String {
        val piece = getPieceAt(from) ?: return "Mueve tu pieza a ${to.algebraic} para mejorar tu juego."
        val targetPiece = getPieceAt(to)
        val pieceName = when (piece.type) {
            PieceType.PAWN -> "el Peón"
            PieceType.KNIGHT -> "el Caballo"
            PieceType.BISHOP -> "el Alfil"
            PieceType.ROOK -> "la Torre"
            PieceType.QUEEN -> "la Dama"
            PieceType.KING -> "el Rey"
        }

        return when {
            targetPiece != null -> "Captura con $pieceName en ${to.algebraic} para ganar material."
            piece.type == PieceType.KING && kotlin.math.abs(from.file - to.file) == 2 -> "Enróquese para proteger al Rey y activar la Torre."
            piece.type == PieceType.PAWN && (to.rank == 3 || to.rank == 4) && (to.file == 3 || to.file == 4) -> "Avanza $pieceName a ${to.algebraic} para controlar el centro."
            piece.type == PieceType.KNIGHT || piece.type == PieceType.BISHOP -> "Desarrolla $pieceName hacia ${to.algebraic} para ganar actividad."
            else -> "Mueve $pieceName a ${to.algebraic} para mejorar tu posición."
        }
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
