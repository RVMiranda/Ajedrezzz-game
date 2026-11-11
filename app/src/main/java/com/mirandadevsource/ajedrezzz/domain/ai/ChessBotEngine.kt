package com.mirandadevsource.ajedrezzz.domain.ai

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.mirandadevsource.ajedrezzz.domain.engine.ChessEngineBridge.Companion.toPosition
import com.mirandadevsource.ajedrezzz.domain.model.Position
import kotlin.random.Random
import com.github.bhlangonijr.chesslib.Piece as LibPiece
import com.github.bhlangonijr.chesslib.PieceType as LibPieceType

class ChessBotEngine {

    /**
     * Calcula la mejor jugada para el bando en turno según la dificultad seleccionada.
     * Retorna Pair(origen, destino) o null si no hay movimientos legales (fin de partida).
     */
    fun findBestMove(board: Board, difficulty: BotDifficulty): Pair<Position, Position>? {
        val clonedBoard = board.clone()
        val legalMoves = clonedBoard.legalMoves()
        if (legalMoves.isEmpty()) return null

        if (legalMoves.size == 1) {
            val single = legalMoves.first()
            return Pair(single.from.toPosition(), single.to.toPosition())
        }

        // Ordenamos las jugadas dando prioridad a capturas (para podar más rápido en alfa-beta)
        val sortedMoves = orderMoves(clonedBoard, legalMoves)

        val depth = difficulty.depth
        val isWhite = clonedBoard.sideToMove == Side.WHITE

        val scoredMoves = mutableListOf<Pair<Move, Int>>()
        var bestScore = Int.MIN_VALUE + 1000

        for (move in sortedMoves) {
            clonedBoard.doMove(move)
            val score = -negamax(
                board = clonedBoard,
                depth = depth - 1,
                alpha = Int.MIN_VALUE + 1000,
                beta = -bestScore
            )
            clonedBoard.undoMove()

            scoredMoves.add(Pair(move, score))
            if (score > bestScore) {
                bestScore = score
            }
        }

        val chosenMove = when (difficulty) {
            BotDifficulty.EASY -> {
                // Modo Fácil: seleccionamos aleatoriamente entre las mejores opciones
                // para simular juego humano no perfecto
                val threshold = bestScore - 120
                val candidateMoves = scoredMoves.filter { it.second >= threshold }
                if (candidateMoves.isNotEmpty()) {
                    candidateMoves[Random.nextInt(candidateMoves.size)].first
                } else {
                    scoredMoves.maxByOrNull { it.second }?.first ?: legalMoves.first()
                }
            }
            BotDifficulty.MEDIUM -> {
                // Modo Medio: seleccionamos la mejor jugada táctica encontrada
                scoredMoves.maxByOrNull { it.second }?.first ?: legalMoves.first()
            }
        }

        return Pair(chosenMove.from.toPosition(), chosenMove.to.toPosition())
    }

    private fun negamax(
        board: Board,
        depth: Int,
        alpha: Int,
        beta: Int
    ): Int {
        if (board.isMated) {
            return -CHECKMATE_SCORE - depth
        }
        if (board.isDraw || board.isStaleMate || board.isInsufficientMaterial) {
            return 0
        }
        if (depth <= 0) {
            return evaluate(board)
        }

        var currentAlpha = alpha
        val legalMoves = orderMoves(board, board.legalMoves())

        for (move in legalMoves) {
            board.doMove(move)
            val score = -negamax(board, depth - 1, -beta, -currentAlpha)
            board.undoMove()

            if (score >= beta) {
                return beta // Corte beta
            }
            if (score > currentAlpha) {
                currentAlpha = score
            }
        }
        return currentAlpha
    }

    private fun orderMoves(board: Board, moves: List<Move>): List<Move> {
        return moves.sortedByDescending { move ->
            val targetPiece = board.getPiece(move.to)
            if (targetPiece != LibPiece.NONE) {
                // MVV-LVA (Most Valuable Victim - Least Valuable Attacker)
                getPieceValue(targetPiece.pieceType) * 10 - getPieceValue(board.getPiece(move.from).pieceType)
            } else {
                0
            }
        }
    }

    /**
     * Evalúa la posición actual desde la perspectiva del bando que tiene el turno.
     */
    fun evaluate(board: Board): Int {
        var whiteScore = 0
        var blackScore = 0

        for (square in Square.values()) {
            if (square == Square.NONE) continue
            val piece = board.getPiece(square)
            if (piece == LibPiece.NONE) continue

            val pieceType = piece.pieceType
            val baseValue = getPieceValue(pieceType)
            val pstValue = getPstValue(pieceType, piece.pieceSide, square)
            val totalPieceScore = baseValue + pstValue

            if (piece.pieceSide == Side.WHITE) {
                whiteScore += totalPieceScore
            } else {
                blackScore += totalPieceScore
            }
        }

        val perspective = if (board.sideToMove == Side.WHITE) 1 else -1
        return perspective * (whiteScore - blackScore)
    }

    private fun getPieceValue(pieceType: LibPieceType): Int {
        return when (pieceType) {
            LibPieceType.PAWN -> 100
            LibPieceType.KNIGHT -> 320
            LibPieceType.BISHOP -> 330
            LibPieceType.ROOK -> 500
            LibPieceType.QUEEN -> 900
            LibPieceType.KING -> 20000
            else -> 0
        }
    }

    private fun getPstValue(pieceType: LibPieceType, side: Side, square: Square): Int {
        val file = square.name[0] - 'A'
        val rawRank = square.name[1] - '1'
        val rank = if (side == Side.WHITE) rawRank else 7 - rawRank
        val index = rank * 8 + file

        return when (pieceType) {
            LibPieceType.PAWN -> PAWN_PST[index]
            LibPieceType.KNIGHT -> KNIGHT_PST[index]
            LibPieceType.BISHOP -> BISHOP_PST[index]
            LibPieceType.ROOK -> ROOK_PST[index]
            LibPieceType.QUEEN -> QUEEN_PST[index]
            LibPieceType.KING -> KING_PST[index]
            else -> 0
        }
    }

    companion object {
        private const val CHECKMATE_SCORE = 30000

        private val PAWN_PST = intArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5, 5, 10, 25, 25, 10, 5, 5,
            0, 0, 0, 20, 20, 0, 0, 0,
            5, -5, -10, 0, 0, -10, -5, 5,
            5, 10, 10, -20, -20, 10, 10, 5,
            0, 0, 0, 0, 0, 0, 0, 0
        )

        private val KNIGHT_PST = intArrayOf(
            -50, -40, -30, -30, -30, -30, -40, -50,
            -40, -20, 0, 0, 0, 0, -20, -40,
            -30, 0, 10, 15, 15, 10, 0, -30,
            -30, 5, 15, 20, 20, 15, 5, -30,
            -30, 0, 15, 20, 20, 15, 0, -30,
            -30, 5, 10, 15, 15, 10, 5, -30,
            -40, -20, 0, 5, 5, 0, -20, -40,
            -50, -40, -30, -30, -30, -30, -40, -50
        )

        private val BISHOP_PST = intArrayOf(
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 5, 5, 10, 10, 5, 5, -10,
            -10, 0, 10, 10, 10, 10, 0, -10,
            -10, 10, 10, 10, 10, 10, 10, -10,
            -10, 5, 0, 0, 0, 0, 5, -10,
            -20, -10, -10, -10, -10, -10, -10, -20
        )

        private val ROOK_PST = intArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0,
            5, 10, 10, 10, 10, 10, 10, 5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            0, 0, 0, 5, 5, 0, 0, 0
        )

        private val QUEEN_PST = intArrayOf(
            -20, -10, -10, -5, -5, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -5, 0, 5, 5, 5, 5, 0, -5,
            0, 0, 5, 5, 5, 5, 0, -5,
            -10, 5, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 0, 0, 0, 0, -10,
            -20, -10, -10, -5, -5, -10, -10, -20
        )

        private val KING_PST = intArrayOf(
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -20, -30, -30, -40, -40, -30, -30, -20,
            -10, -20, -20, -20, -20, -20, -20, -10,
            20, 20, 0, 0, 0, 0, 20, 20,
            20, 30, 10, 0, 0, 10, 30, 20
        )
    }
}
