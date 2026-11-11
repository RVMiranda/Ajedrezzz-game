package com.mirandadevsource.ajedrezzz.domain.ai

import com.github.bhlangonijr.chesslib.Board
import com.mirandadevsource.ajedrezzz.domain.engine.ChessEngineBridge
import com.mirandadevsource.ajedrezzz.domain.model.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChessBotEngineTest {

    private lateinit var botEngine: ChessBotEngine

    @Before
    fun setUp() {
        botEngine = ChessBotEngine()
    }

    @Test
    fun `initial board evaluation is balanced`() {
        val board = Board()
        val score = botEngine.evaluate(board)
        // Symmetric starting position should be close to 0
        assertEquals(0, score)
    }

    @Test
    fun `findBestMove returns a legal move on initial board`() {
        val board = Board()
        val bestMove = botEngine.findBestMove(board, BotDifficulty.EASY)

        assertNotNull(bestMove)
        val bridge = ChessEngineBridge(board)
        val legalMoves = bridge.getLegalMovesFrom(bestMove!!.first)
        assertTrue(legalMoves.contains(bestMove.second))
    }

    @Test
    fun `bot captures undefended piece when available`() {
        // Setup a position where Black has a free pawn to capture
        val bridge = ChessEngineBridge()
        // 1. e4 d5 2. a3 dxe4 (Black pawn can take e4)
        bridge.makeMove(Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"))
        bridge.makeMove(Position.fromAlgebraic("d7"), Position.fromAlgebraic("d5"))
        bridge.makeMove(Position.fromAlgebraic("a2"), Position.fromAlgebraic("a3"))

        // Turn is now Black, best move should be capturing the free pawn on e4
        val bestMove = bridge.findBestBotMove(BotDifficulty.MEDIUM)
        assertNotNull(bestMove)
        assertEquals(Position.fromAlgebraic("d5"), bestMove?.first)
        assertEquals(Position.fromAlgebraic("e4"), bestMove?.second)
    }
}
