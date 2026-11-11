package com.mirandadevsource.ajedrezzz.ui.game

import androidx.lifecycle.ViewModel
import com.mirandadevsource.ajedrezzz.domain.engine.ChessEngineBridge
import com.mirandadevsource.ajedrezzz.domain.model.GameStatus
import com.mirandadevsource.ajedrezzz.domain.model.MoveResult
import com.mirandadevsource.ajedrezzz.domain.model.Piece
import com.mirandadevsource.ajedrezzz.domain.model.PieceColor
import com.mirandadevsource.ajedrezzz.domain.model.Position
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ChessViewModel(
    private val bridge: ChessEngineBridge = ChessEngineBridge()
) : ViewModel() {

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<ChessUiState> = _uiState.asStateFlow()

    private fun createInitialState(): ChessUiState {
        val (whiteAdv, blackAdv) = bridge.getMaterialAdvantage()
        return ChessUiState(
            squares = bridge.getAllSquares(),
            selectedPosition = null,
            sideToMove = bridge.sideToMove,
            gameStatus = bridge.getGameStatus(),
            gameMode = GameMode.PASS_AND_PLAY,
            isFlipped = false,
            isAutoFlipEnabled = false,
            canUndo = bridge.canUndo,
            showThreats = false,
            whiteMaterialAdvantage = whiteAdv,
            blackMaterialAdvantage = blackAdv,
            statusMessage = getStatusText(bridge.getGameStatus(), bridge.sideToMove)
        )
    }

    fun onEvent(event: ChessUiEvent) {
        when (event) {
            is ChessUiEvent.SquareClicked -> handleSquareClick(event.position)
            is ChessUiEvent.UndoClicked -> handleUndo()
            is ChessUiEvent.ResetGameClicked -> handleReset()
            is ChessUiEvent.ToggleFlipClicked -> handleToggleFlip()
            is ChessUiEvent.ToggleAutoFlipClicked -> handleToggleAutoFlip()
            is ChessUiEvent.ToggleThreatsClicked -> handleToggleThreats()
            is ChessUiEvent.ChangeGameMode -> handleChangeGameMode(event.mode)
        }
    }

    private fun handleToggleThreats() {
        _uiState.update { state ->
            val nextThreats = !state.showThreats
            state.copy(
                showThreats = nextThreats,
                squares = bridge.getAllSquares(
                    selectedPosition = state.selectedPosition,
                    showThreats = nextThreats
                )
            )
        }
    }

    private fun handleSquareClick(position: Position) {
        val currentState = _uiState.value
        if (currentState.gameStatus.isGameOver) return

        val selected = currentState.selectedPosition
        val pieceAtClicked = bridge.getPieceAt(position)

        if (selected == null) {
            // No hay casilla seleccionada: seleccionamos si es pieza del turno actual
            if (pieceAtClicked != null && pieceAtClicked.color == bridge.sideToMove) {
                _uiState.update { state ->
                    state.copy(
                        selectedPosition = position,
                        squares = bridge.getAllSquares(
                            selectedPosition = position,
                            showThreats = state.showThreats
                        )
                    )
                }
            }
        } else {
            // Ya había una casilla seleccionada
            if (selected == position) {
                // Click en la misma casilla: deseleccionar
                clearSelection()
            } else if (pieceAtClicked != null && pieceAtClicked.color == bridge.sideToMove) {
                // Click en otra pieza amiga: cambiar la selección
                _uiState.update { state ->
                    state.copy(
                        selectedPosition = position,
                        squares = bridge.getAllSquares(
                            selectedPosition = position,
                            showThreats = state.showThreats
                        )
                    )
                }
            } else {
                // Click en casilla destino: verificar si es legal
                val legalDestinations = bridge.getLegalMovesFrom(selected)
                if (legalDestinations.contains(position)) {
                    executeMove(from = selected, to = position)
                } else {
                    // Click en casilla no permitida: deseleccionar
                    clearSelection()
                }
            }
        }
    }

    private fun executeMove(from: Position, to: Position) {
        val captured = bridge.getPieceAt(to)
        val movingColor = bridge.sideToMove
        val result = bridge.makeMove(from, to)

        if (result is MoveResult.Success) {
            val newCapturedWhite = _uiState.value.capturedByWhite.toMutableList()
            val newCapturedBlack = _uiState.value.capturedByBlack.toMutableList()

            if (captured != null) {
                if (movingColor == PieceColor.WHITE) {
                    newCapturedWhite.add(captured)
                } else {
                    newCapturedBlack.add(captured)
                }
            }

            val newGameStatus = bridge.getGameStatus()
            val newSideToMove = bridge.sideToMove
            val (whiteAdv, blackAdv) = bridge.getMaterialAdvantage()
            val shouldAutoFlip = _uiState.value.isAutoFlipEnabled &&
                    _uiState.value.gameMode == GameMode.PASS_AND_PLAY

            val newIsFlipped = if (shouldAutoFlip) {
                newSideToMove == PieceColor.BLACK
            } else {
                _uiState.value.isFlipped
            }

            _uiState.update { state ->
                state.copy(
                    squares = bridge.getAllSquares(showThreats = state.showThreats),
                    selectedPosition = null,
                    sideToMove = newSideToMove,
                    gameStatus = newGameStatus,
                    canUndo = bridge.canUndo,
                    statusMessage = getStatusText(newGameStatus, newSideToMove),
                    capturedByWhite = newCapturedWhite,
                    capturedByBlack = newCapturedBlack,
                    whiteMaterialAdvantage = whiteAdv,
                    blackMaterialAdvantage = blackAdv,
                    lastMoveFrom = from,
                    lastMoveTo = to,
                    isFlipped = newIsFlipped
                )
            }
        } else {
            clearSelection()
        }
    }

    private fun handleUndo() {
        if (!bridge.canUndo) return
        val undone = bridge.undoMove()
        if (undone) {
            val status = bridge.getGameStatus()
            val side = bridge.sideToMove
            val (whiteAdv, blackAdv) = bridge.getMaterialAdvantage()
            val shouldAutoFlip = _uiState.value.isAutoFlipEnabled &&
                    _uiState.value.gameMode == GameMode.PASS_AND_PLAY

            _uiState.update { state ->
                state.copy(
                    squares = bridge.getAllSquares(showThreats = state.showThreats),
                    selectedPosition = null,
                    sideToMove = side,
                    gameStatus = status,
                    canUndo = bridge.canUndo,
                    statusMessage = getStatusText(status, side),
                    whiteMaterialAdvantage = whiteAdv,
                    blackMaterialAdvantage = blackAdv,
                    lastMoveFrom = null,
                    lastMoveTo = null,
                    isFlipped = if (shouldAutoFlip) side == PieceColor.BLACK else state.isFlipped
                )
            }
        }
    }

    private fun handleReset() {
        bridge.reset()
        _uiState.value = createInitialState().copy(
            gameMode = _uiState.value.gameMode,
            isFlipped = _uiState.value.isFlipped,
            isAutoFlipEnabled = _uiState.value.isAutoFlipEnabled,
            showThreats = _uiState.value.showThreats
        )
    }

    private fun handleToggleFlip() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    private fun handleToggleAutoFlip() {
        _uiState.update { state ->
            val nextAutoFlip = !state.isAutoFlipEnabled
            val newFlipped = if (nextAutoFlip && state.gameMode == GameMode.PASS_AND_PLAY) {
                state.sideToMove == PieceColor.BLACK
            } else {
                state.isFlipped
            }
            state.copy(
                isAutoFlipEnabled = nextAutoFlip,
                isFlipped = newFlipped
            )
        }
    }

    private fun handleChangeGameMode(mode: GameMode) {
        _uiState.update { it.copy(gameMode = mode) }
    }

    private fun clearSelection() {
        _uiState.update { state ->
            state.copy(
                selectedPosition = null,
                squares = bridge.getAllSquares(
                    selectedPosition = null,
                    showThreats = state.showThreats
                )
            )
        }
    }

    private fun getStatusText(status: GameStatus, sideToMove: PieceColor): String {
        return when (status) {
            GameStatus.IN_PROGRESS -> if (sideToMove == PieceColor.WHITE) "Turno de las Blancas" else "Turno de las Negras"
            GameStatus.CHECK -> if (sideToMove == PieceColor.WHITE) "¡Jaque! Turno de las Blancas" else "¡Jaque! Turno de las Negras"
            GameStatus.CHECKMATE -> if (sideToMove == PieceColor.WHITE) "¡Jaque Mate! Ganan las Negras" else "¡Jaque Mate! Ganan las Blancas"
            GameStatus.STALEMATE -> "Tablas por Rey Ahogado"
            GameStatus.DRAW_INSUFFICIENT_MATERIAL -> "Tablas por Material Insuficiente"
            GameStatus.DRAW_REPETITION -> "Tablas por Triple Repetición"
            GameStatus.DRAW_50_MOVES -> "Tablas por Regla de los 50 Movimientos"
        }
    }
}
