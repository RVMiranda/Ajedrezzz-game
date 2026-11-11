package com.mirandadevsource.ajedrezzz.domain

import com.mirandadevsource.ajedrezzz.domain.engine.ChessEngineBridge
import com.mirandadevsource.ajedrezzz.domain.model.GameStatus
import com.mirandadevsource.ajedrezzz.domain.model.MoveResult
import com.mirandadevsource.ajedrezzz.domain.model.PieceColor
import com.mirandadevsource.ajedrezzz.domain.model.PieceType
import com.mirandadevsource.ajedrezzz.domain.model.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChessEngineBridgeTest {

    private lateinit var bridge: ChessEngineBridge

    @Before
    fun setUp() {
        bridge = ChessEngineBridge()
    }

    @Test
    fun `initial board state has white to move and correct pieces`() {
        assertEquals(PieceColor.WHITE, bridge.sideToMove)
        assertFalse(bridge.canUndo)
        assertFalse(bridge.isKingInCheck())
        assertEquals(GameStatus.IN_PROGRESS, bridge.getGameStatus())

        // Check white pawns on rank 1 (rank index 1 = row 2)
        for (file in 0..7) {
            val piece = bridge.getPieceAt(Position(file, 1))
            assertNotNull(piece)
            assertEquals(PieceType.PAWN, piece?.type)
            assertEquals(PieceColor.WHITE, piece?.color)
        }

        // Check white King on e1
        val whiteKing = bridge.getPieceAt(Position.fromAlgebraic("e1"))
        assertNotNull(whiteKing)
        assertEquals(PieceType.KING, whiteKing?.type)
        assertEquals(PieceColor.WHITE, whiteKing?.color)

        // Check black King on e8
        val blackKing = bridge.getPieceAt(Position.fromAlgebraic("e8"))
        assertNotNull(blackKing)
        assertEquals(PieceType.KING, blackKing?.type)
        assertEquals(PieceColor.BLACK, blackKing?.color)
    }

    @Test
    fun `legal moves for initial pawn e2`() {
        val e2 = Position.fromAlgebraic("e2")
        val legalMoves = bridge.getLegalMovesFrom(e2)

        // e2 pawn can move to e3 or e4
        assertEquals(2, legalMoves.size)
        assertTrue(legalMoves.contains(Position.fromAlgebraic("e3")))
        assertTrue(legalMoves.contains(Position.fromAlgebraic("e4")))
    }

    @Test
    fun `make move updates board and switches turn`() {
        val e2 = Position.fromAlgebraic("e2")
        val e4 = Position.fromAlgebraic("e4")

        val result = bridge.makeMove(e2, e4)
        assertTrue(result is MoveResult.Success)

        // e2 should now be empty
        assertNull(bridge.getPieceAt(e2))

        // e4 should now have White Pawn
        val pieceAtE4 = bridge.getPieceAt(e4)
        assertNotNull(pieceAtE4)
        assertEquals(PieceType.PAWN, pieceAtE4?.type)
        assertEquals(PieceColor.WHITE, pieceAtE4?.color)

        // Turn should now be Black
        assertEquals(PieceColor.BLACK, bridge.sideToMove)
        assertTrue(bridge.canUndo)
    }

    @Test
    fun `illegal move is rejected`() {
        val e2 = Position.fromAlgebraic("e2")
        val e5 = Position.fromAlgebraic("e5") // Pawn cannot move 3 squares

        val result = bridge.makeMove(e2, e5)
        assertTrue(result is MoveResult.IllegalMove)
        assertEquals(PieceColor.WHITE, bridge.sideToMove)
    }

    @Test
    fun `undo move reverts to previous state`() {
        val e2 = Position.fromAlgebraic("e2")
        val e4 = Position.fromAlgebraic("e4")

        bridge.makeMove(e2, e4)
        assertEquals(PieceColor.BLACK, bridge.sideToMove)

        val undone = bridge.undoMove()
        assertTrue(undone)
        assertEquals(PieceColor.WHITE, bridge.sideToMove)
        assertNotNull(bridge.getPieceAt(e2))
        assertNull(bridge.getPieceAt(e4))
        assertFalse(bridge.canUndo)
    }

    @Test
    fun `fools mate results in checkmate`() {
        // 1. f3 e5
        bridge.makeMove(Position.fromAlgebraic("f2"), Position.fromAlgebraic("f3"))
        bridge.makeMove(Position.fromAlgebraic("e7"), Position.fromAlgebraic("e5"))

        // 2. g4 Qh4#
        bridge.makeMove(Position.fromAlgebraic("g2"), Position.fromAlgebraic("g4"))
        val result = bridge.makeMove(Position.fromAlgebraic("d8"), Position.fromAlgebraic("h4"))

        assertTrue(result is MoveResult.Success)
        assertTrue(bridge.isKingInCheck())
        assertEquals(GameStatus.CHECKMATE, bridge.getGameStatus())
    }

    @Test
    fun `threatened pieces detection identifies attacked pieces`() {
        // 1. e4 d5 (Black pawn attacks White pawn on e4, White pawn attacks Black pawn on d5)
        bridge.makeMove(Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"))
        bridge.makeMove(Position.fromAlgebraic("d7"), Position.fromAlgebraic("d5"))

        // Turn is now White
        val whiteThreatened = bridge.getThreatenedPieces(PieceColor.WHITE)
        assertTrue(whiteThreatened.contains(Position.fromAlgebraic("e4")))

        val blackThreatened = bridge.getThreatenedPieces(PieceColor.BLACK)
        assertTrue(blackThreatened.contains(Position.fromAlgebraic("d5")))
    }
}
