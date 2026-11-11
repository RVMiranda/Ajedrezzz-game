package com.mirandadevsource.ajedrezzz.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MoveNotationHelperTest {

    @Test
    fun `pawn advance formats correctly`() {
        val pawn = Piece(PieceType.PAWN, PieceColor.WHITE)
        val notation = MoveNotationHelper.formatMove(
            piece = pawn,
            from = Position.fromAlgebraic("e2"),
            to = Position.fromAlgebraic("e4"),
            isCapture = false,
            isCheck = false,
            isCheckmate = false
        )
        assertEquals("e4", notation)
    }

    @Test
    fun `pawn capture formats with file prefix`() {
        val pawn = Piece(PieceType.PAWN, PieceColor.WHITE)
        val notation = MoveNotationHelper.formatMove(
            piece = pawn,
            from = Position.fromAlgebraic("e4"),
            to = Position.fromAlgebraic("d5"),
            isCapture = true,
            isCheck = false,
            isCheckmate = false
        )
        assertEquals("exd5", notation)
    }

    @Test
    fun `knight move formats with Spanish letter C`() {
        val knight = Piece(PieceType.KNIGHT, PieceColor.WHITE)
        val notation = MoveNotationHelper.formatMove(
            piece = knight,
            from = Position.fromAlgebraic("g1"),
            to = Position.fromAlgebraic("f3"),
            isCapture = false,
            isCheck = false,
            isCheckmate = false
        )
        assertEquals("Cf3", notation)
    }

    @Test
    fun `move with checkmate has hash suffix`() {
        val queen = Piece(PieceType.QUEEN, PieceColor.BLACK)
        val notation = MoveNotationHelper.formatMove(
            piece = queen,
            from = Position.fromAlgebraic("d8"),
            to = Position.fromAlgebraic("h4"),
            isCapture = false,
            isCheck = true,
            isCheckmate = true
        )
        assertEquals("Dh4#", notation)
    }

    @Test
    fun `pawn promotion formats with equal sign`() {
        val pawn = Piece(PieceType.PAWN, PieceColor.WHITE)
        val notation = MoveNotationHelper.formatMove(
            piece = pawn,
            from = Position.fromAlgebraic("e7"),
            to = Position.fromAlgebraic("e8"),
            isCapture = false,
            isCheck = false,
            isCheckmate = false,
            promotionPiece = PieceType.QUEEN
        )
        assertEquals("e8=D", notation)
    }

    @Test
    fun `kingside castling formats as 0-0`() {
        val king = Piece(PieceType.KING, PieceColor.WHITE)
        val notation = MoveNotationHelper.formatMove(
            piece = king,
            from = Position.fromAlgebraic("e1"),
            to = Position.fromAlgebraic("g1"),
            isCapture = false,
            isCheck = false,
            isCheckmate = false
        )
        assertEquals("0-0", notation)
    }
}
