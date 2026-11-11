package com.mirandadevsource.ajedrezzz.ui

import com.mirandadevsource.ajedrezzz.domain.model.GameStatus
import com.mirandadevsource.ajedrezzz.domain.model.PieceColor
import com.mirandadevsource.ajedrezzz.domain.model.Position
import com.mirandadevsource.ajedrezzz.ui.game.ChessUiEvent
import com.mirandadevsource.ajedrezzz.ui.game.ChessViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChessViewModelTest {

    private lateinit var viewModel: ChessViewModel

    @Before
    fun setUp() {
        viewModel = ChessViewModel()
    }

    @Test
    fun `initial state has correct defaults`() {
        val state = viewModel.uiState.value
        assertEquals(PieceColor.WHITE, state.sideToMove)
        assertNull(state.selectedPosition)
        assertEquals(GameStatus.IN_PROGRESS, state.gameStatus)
        assertFalse(state.canUndo)
        assertFalse(state.isFlipped)
        assertFalse(state.isAutoFlipEnabled)
        assertEquals("Turno de las Blancas", state.statusMessage)
        assertEquals(64, state.squares.size)
    }

    @Test
    fun `clicking friendly piece selects it and highlights destinations`() {
        val e2 = Position.fromAlgebraic("e2")
        viewModel.onEvent(ChessUiEvent.SquareClicked(e2))

        val state = viewModel.uiState.value
        assertEquals(e2, state.selectedPosition)

        val e2Square = state.squares.first { it.position == e2 }
        assertTrue(e2Square.isSelected)

        val e3Square = state.squares.first { it.position == Position.fromAlgebraic("e3") }
        val e4Square = state.squares.first { it.position == Position.fromAlgebraic("e4") }
        assertTrue(e3Square.isLegalDestination)
        assertTrue(e4Square.isLegalDestination)
    }

    @Test
    fun `clicking empty square initially does not select anything`() {
        val e4 = Position.fromAlgebraic("e4")
        viewModel.onEvent(ChessUiEvent.SquareClicked(e4))

        val state = viewModel.uiState.value
        assertNull(state.selectedPosition)
    }

    @Test
    fun `clicking same selected piece deselects it`() {
        val e2 = Position.fromAlgebraic("e2")
        viewModel.onEvent(ChessUiEvent.SquareClicked(e2))
        assertEquals(e2, viewModel.uiState.value.selectedPosition)

        viewModel.onEvent(ChessUiEvent.SquareClicked(e2))
        assertNull(viewModel.uiState.value.selectedPosition)
    }

    @Test
    fun `clicking another friendly piece switches selection`() {
        val e2 = Position.fromAlgebraic("e2")
        val d2 = Position.fromAlgebraic("d2")

        viewModel.onEvent(ChessUiEvent.SquareClicked(e2))
        assertEquals(e2, viewModel.uiState.value.selectedPosition)

        viewModel.onEvent(ChessUiEvent.SquareClicked(d2))
        assertEquals(d2, viewModel.uiState.value.selectedPosition)
    }

    @Test
    fun `clicking legal destination executes move and switches turn to Black`() {
        val e2 = Position.fromAlgebraic("e2")
        val e4 = Position.fromAlgebraic("e4")

        viewModel.onEvent(ChessUiEvent.SquareClicked(e2))
        viewModel.onEvent(ChessUiEvent.SquareClicked(e4))

        val state = viewModel.uiState.value
        assertNull(state.selectedPosition)
        assertEquals(PieceColor.BLACK, state.sideToMove)
        assertTrue(state.canUndo)
        assertEquals("Turno de las Negras", state.statusMessage)
        assertEquals(e2, state.lastMoveFrom)
        assertEquals(e4, state.lastMoveTo)

        val e4Square = state.squares.first { it.position == e4 }
        assertNotNull(e4Square.piece)
        assertEquals(PieceColor.WHITE, e4Square.piece?.color)
    }

    @Test
    fun `pass and play mode allows Black to respond immediately`() {
        // 1. Blancas: e2 -> e4
        viewModel.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e2")))
        viewModel.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e4")))

        // 2. Negras: e7 -> e5
        viewModel.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e7")))
        viewModel.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e5")))

        val state = viewModel.uiState.value
        assertEquals(PieceColor.WHITE, state.sideToMove)
        assertEquals("Turno de las Blancas", state.statusMessage)

        val e5Square = state.squares.first { it.position == Position.fromAlgebraic("e5") }
        assertNotNull(e5Square.piece)
        assertEquals(PieceColor.BLACK, e5Square.piece?.color)
    }

    @Test
    fun `undo button reverts last move`() {
        val e2 = Position.fromAlgebraic("e2")
        val e4 = Position.fromAlgebraic("e4")

        viewModel.onEvent(ChessUiEvent.SquareClicked(e2))
        viewModel.onEvent(ChessUiEvent.SquareClicked(e4))
        assertEquals(PieceColor.BLACK, viewModel.uiState.value.sideToMove)

        viewModel.onEvent(ChessUiEvent.UndoClicked)
        val state = viewModel.uiState.value
        assertEquals(PieceColor.WHITE, state.sideToMove)
        assertFalse(state.canUndo)
        assertEquals("Turno de las Blancas", state.statusMessage)
    }

    @Test
    fun `toggle flip and auto-flip update orientation state`() {
        assertFalse(viewModel.uiState.value.isFlipped)
        viewModel.onEvent(ChessUiEvent.ToggleFlipClicked)
        assertTrue(viewModel.uiState.value.isFlipped)

        assertFalse(viewModel.uiState.value.isAutoFlipEnabled)
        viewModel.onEvent(ChessUiEvent.ToggleAutoFlipClicked)
        assertTrue(viewModel.uiState.value.isAutoFlipEnabled)
    }

    @Test
    fun `reset game restores initial state`() {
        viewModel.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e2")))
        viewModel.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e4")))

        viewModel.onEvent(ChessUiEvent.ResetGameClicked)
        val state = viewModel.uiState.value
        assertEquals(PieceColor.WHITE, state.sideToMove)
        assertFalse(state.canUndo)
        assertNull(state.lastMoveFrom)
    }
}
