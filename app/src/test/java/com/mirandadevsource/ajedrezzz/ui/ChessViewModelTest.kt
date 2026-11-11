package com.mirandadevsource.ajedrezzz.ui

import com.mirandadevsource.ajedrezzz.domain.ai.BotDifficulty
import com.mirandadevsource.ajedrezzz.domain.engine.ChessEngineBridge
import com.mirandadevsource.ajedrezzz.domain.model.GameStatus
import com.mirandadevsource.ajedrezzz.domain.model.PieceColor
import com.mirandadevsource.ajedrezzz.domain.model.PieceType
import com.mirandadevsource.ajedrezzz.domain.model.Position
import com.mirandadevsource.ajedrezzz.domain.storage.InMemoryGameStorage
import com.mirandadevsource.ajedrezzz.ui.game.AppScreen
import com.mirandadevsource.ajedrezzz.ui.game.ChessUiEvent
import com.mirandadevsource.ajedrezzz.ui.game.ChessViewModel
import com.mirandadevsource.ajedrezzz.ui.game.GameMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChessViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ChessViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ChessViewModel(botDispatcher = testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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
        assertEquals(GameMode.PASS_AND_PLAY, state.gameMode)
        assertEquals(BotDifficulty.EASY, state.botDifficulty)
        assertFalse(state.isBotThinking)
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

    @Test
    fun `toggle threats enables tutor mode and highlights attacked pieces`() {
        assertFalse(viewModel.uiState.value.showThreats)

        // 1. e4 d5
        viewModel.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e2")))
        viewModel.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e4")))
        viewModel.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("d7")))
        viewModel.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("d5")))

        // Turno de Blancas: activamos el Modo Tutor
        viewModel.onEvent(ChessUiEvent.ToggleThreatsClicked)
        assertTrue(viewModel.uiState.value.showThreats)

        // El peón blanco en e4 está amenazado por el peón negro de d5
        val e4Square = viewModel.uiState.value.squares.first { it.position == Position.fromAlgebraic("e4") }
        assertTrue(e4Square.isThreatened)
    }

    @Test
    fun `capturing pieces calculates material advantage correctly and undo reverts it`() {
        assertEquals(0, viewModel.uiState.value.whiteMaterialAdvantage)
        assertEquals(0, viewModel.uiState.value.blackMaterialAdvantage)

        // 1. e4 d5
        viewModel.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e2")))
        viewModel.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e4")))
        viewModel.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("d7")))
        viewModel.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("d5")))

        // 2. exd5 (Blancas capturan peón en d5: peón vale 1 punto)
        viewModel.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e4")))
        viewModel.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("d5")))

        val stateAfterCapture = viewModel.uiState.value
        assertEquals(1, stateAfterCapture.whiteMaterialAdvantage)
        assertEquals(0, stateAfterCapture.blackMaterialAdvantage)
        assertEquals(1, stateAfterCapture.capturedByWhite.size)

        // Deshacemos la captura
        viewModel.onEvent(ChessUiEvent.UndoClicked)
        val stateAfterUndo = viewModel.uiState.value
        assertEquals(0, stateAfterUndo.whiteMaterialAdvantage)
        assertEquals(0, stateAfterUndo.blackMaterialAdvantage)
    }

    @Test
    fun `change game mode and bot difficulty`() {
        viewModel.onEvent(ChessUiEvent.ChangeGameMode(GameMode.VS_BOT))
        assertEquals(GameMode.VS_BOT, viewModel.uiState.value.gameMode)

        viewModel.onEvent(ChessUiEvent.ChangeBotDifficulty(BotDifficulty.MEDIUM))
        assertEquals(BotDifficulty.MEDIUM, viewModel.uiState.value.botDifficulty)
    }

    @Test
    fun `in vs bot mode bot responds after player move and undo reverts both moves`() = runTest {
        viewModel.onEvent(ChessUiEvent.ChangeGameMode(GameMode.VS_BOT))

        // White plays e2-e4
        viewModel.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e2")))
        viewModel.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e4")))

        // Advance coroutines so delay(450) and bot calculation complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Turn is White again after bot responds
        assertEquals(PieceColor.WHITE, viewModel.uiState.value.sideToMove)
        assertTrue(viewModel.uiState.value.canUndo)

        // Undo against bot reverts 2 moves back to start
        viewModel.onEvent(ChessUiEvent.UndoClicked)
        val stateAfterUndo = viewModel.uiState.value
        assertEquals(PieceColor.WHITE, stateAfterUndo.sideToMove)
        assertFalse(stateAfterUndo.canUndo)
        assertNull(stateAfterUndo.lastMoveFrom)
    }

    @Test
    fun `pawn promotion triggers pendingPromotion and promotes on selection`() {
        val bridge = ChessEngineBridge()
        bridge.loadFen("8/4P3/8/8/8/8/8/4K2k w - - 0 1")
        val vm = ChessViewModel(bridge = bridge, botDispatcher = testDispatcher)

        val e7 = Position.fromAlgebraic("e7")
        val e8 = Position.fromAlgebraic("e8")

        vm.onEvent(ChessUiEvent.SquareClicked(e7))
        vm.onEvent(ChessUiEvent.SquareClicked(e8))

        // Should be waiting for promotion selection
        assertEquals(Pair(e7, e8), vm.uiState.value.pendingPromotion)

        // Select Queen
        vm.onEvent(ChessUiEvent.PromotionSelected(PieceType.QUEEN))

        // Promotion complete
        assertNull(vm.uiState.value.pendingPromotion)
        val e8Square = vm.uiState.value.squares.first { it.position == e8 }
        assertEquals(PieceType.QUEEN, e8Square.piece?.type)
        assertEquals(PieceColor.WHITE, e8Square.piece?.color)
        assertEquals("e8=D", vm.uiState.value.moveHistory.first().whiteMove)
    }

    @Test
    fun `start new game from main menu switches screen to game with selected mode`() {
        assertEquals(AppScreen.MAIN_MENU, viewModel.uiState.value.currentScreen)

        viewModel.onEvent(ChessUiEvent.StartNewGame(GameMode.VS_BOT, BotDifficulty.MEDIUM))

        val state = viewModel.uiState.value
        assertEquals(AppScreen.GAME, state.currentScreen)
        assertEquals(GameMode.VS_BOT, state.gameMode)
        assertEquals(BotDifficulty.MEDIUM, state.botDifficulty)
        assertFalse(state.showPauseDialog)
    }

    @Test
    fun `pause and resume game toggles pause dialog`() {
        viewModel.onEvent(ChessUiEvent.StartNewGame(GameMode.PASS_AND_PLAY))
        assertFalse(viewModel.uiState.value.showPauseDialog)

        viewModel.onEvent(ChessUiEvent.PauseClicked)
        assertTrue(viewModel.uiState.value.showPauseDialog)

        viewModel.onEvent(ChessUiEvent.ResumeGameClicked)
        assertFalse(viewModel.uiState.value.showPauseDialog)
    }

    @Test
    fun `suspend game saves state and returns to main menu with suspended game available`() {
        val storage = InMemoryGameStorage()
        val vm = ChessViewModel(gameStorage = storage, botDispatcher = testDispatcher)

        // Start game and make 1 move (e2 -> e4)
        vm.onEvent(ChessUiEvent.StartNewGame(GameMode.PASS_AND_PLAY))
        vm.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e2")))
        vm.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e4")))

        // Pause and suspend
        vm.onEvent(ChessUiEvent.PauseClicked)
        vm.onEvent(ChessUiEvent.SuspendAndExitClicked)

        // Verifications
        assertEquals(AppScreen.MAIN_MENU, vm.uiState.value.currentScreen)
        assertTrue(vm.uiState.value.hasSuspendedGame)
        assertFalse(vm.uiState.value.showPauseDialog)
        assertTrue(storage.hasSavedGame())

        val saved = storage.loadGame()
        assertNotNull(saved)
        assertEquals(1, saved?.moveHistory?.size)
        assertEquals("e4", saved?.moveHistory?.first()?.whiteMove)
    }

    @Test
    fun `resuming suspended game restores exact position and history`() {
        val storage = InMemoryGameStorage()
        val vm1 = ChessViewModel(gameStorage = storage, botDispatcher = testDispatcher)

        vm1.onEvent(ChessUiEvent.StartNewGame(GameMode.PASS_AND_PLAY))
        vm1.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e2")))
        vm1.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e4")))
        vm1.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("d7")))
        vm1.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("d5")))
        vm1.onEvent(ChessUiEvent.SuspendAndExitClicked)

        // Create fresh ViewModel with the same storage (simulating app restart)
        val vm2 = ChessViewModel(gameStorage = storage, botDispatcher = testDispatcher)
        assertTrue(vm2.uiState.value.hasSuspendedGame)
        assertEquals(AppScreen.MAIN_MENU, vm2.uiState.value.currentScreen)

        // Resume game
        vm2.onEvent(ChessUiEvent.ResumeSuspendedGame)

        val resumedState = vm2.uiState.value
        assertEquals(AppScreen.GAME, resumedState.currentScreen)
        assertEquals(PieceColor.WHITE, resumedState.sideToMove)
        assertEquals(1, resumedState.moveHistory.size)
        assertEquals("e4", resumedState.moveHistory.first().whiteMove)
        assertEquals("d5", resumedState.moveHistory.first().blackMove)

        val e4Square = resumedState.squares.first { it.position == Position.fromAlgebraic("e4") }
        assertEquals(PieceType.PAWN, e4Square.piece?.type)
        assertEquals(PieceColor.WHITE, e4Square.piece?.color)

        val d5Square = resumedState.squares.first { it.position == Position.fromAlgebraic("d5") }
        assertEquals(PieceType.PAWN, d5Square.piece?.type)
        assertEquals(PieceColor.BLACK, d5Square.piece?.color)
    }

    @Test
    fun `resigning game marks game over and clears suspended game`() {
        val storage = InMemoryGameStorage()
        val vm = ChessViewModel(gameStorage = storage, botDispatcher = testDispatcher)

        vm.onEvent(ChessUiEvent.StartNewGame(GameMode.PASS_AND_PLAY))
        vm.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e2")))
        vm.onEvent(ChessUiEvent.SquareClicked(Position.fromAlgebraic("e4")))
        // Suspend
        vm.onEvent(ChessUiEvent.SuspendAndExitClicked)
        assertTrue(storage.hasSavedGame())

        // Resume and resign
        vm.onEvent(ChessUiEvent.ResumeSuspendedGame)
        vm.onEvent(ChessUiEvent.PauseClicked)
        vm.onEvent(ChessUiEvent.ResignGameClicked)

        val state = vm.uiState.value
        assertTrue(state.gameStatus.isGameOver)
        assertEquals(GameStatus.CHECKMATE, state.gameStatus)
        assertFalse(storage.hasSavedGame())
        assertFalse(state.hasSuspendedGame)
        assertFalse(state.showPauseDialog)
    }

    @Test
    fun `return to main menu navigates back`() {
        viewModel.onEvent(ChessUiEvent.StartNewGame(GameMode.PASS_AND_PLAY))
        assertEquals(AppScreen.GAME, viewModel.uiState.value.currentScreen)

        viewModel.onEvent(ChessUiEvent.ReturnToMainMenuClicked)
        assertEquals(AppScreen.MAIN_MENU, viewModel.uiState.value.currentScreen)
    }
}
