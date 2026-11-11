package com.mirandadevsource.ajedrezzz.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mirandadevsource.ajedrezzz.domain.ai.BotDifficulty
import com.mirandadevsource.ajedrezzz.domain.engine.ChessEngineBridge
import com.mirandadevsource.ajedrezzz.domain.model.GameStatus
import com.mirandadevsource.ajedrezzz.domain.model.MoveLogItem
import com.mirandadevsource.ajedrezzz.domain.model.MoveNotationHelper
import com.mirandadevsource.ajedrezzz.domain.model.MoveResult
import com.mirandadevsource.ajedrezzz.domain.model.Piece
import com.mirandadevsource.ajedrezzz.domain.model.PieceColor
import com.mirandadevsource.ajedrezzz.domain.model.PieceType
import com.mirandadevsource.ajedrezzz.domain.model.Position
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChessViewModel(
    private val bridge: ChessEngineBridge = ChessEngineBridge(),
    private val botDispatcher: CoroutineDispatcher = Dispatchers.Default
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
            botDifficulty = BotDifficulty.EASY,
            isBotThinking = false,
            isFlipped = false,
            isAutoFlipEnabled = false,
            canUndo = bridge.canUndo,
            showThreats = false,
            whiteMaterialAdvantage = whiteAdv,
            blackMaterialAdvantage = blackAdv,
            pendingPromotion = null,
            moveHistory = emptyList(),
            statusMessage = getStatusText(bridge.getGameStatus(), bridge.sideToMove)
        )
    }

    fun onEvent(event: ChessUiEvent) {
        when (event) {
            is ChessUiEvent.SquareClicked -> handleSquareClick(event.position)
            is ChessUiEvent.PromotionSelected -> handlePromotionSelected(event.pieceType)
            is ChessUiEvent.PromotionDismissed -> handlePromotionDismissed()
            is ChessUiEvent.UndoClicked -> handleUndo()
            is ChessUiEvent.ResetGameClicked -> handleReset()
            is ChessUiEvent.ToggleFlipClicked -> handleToggleFlip()
            is ChessUiEvent.ToggleAutoFlipClicked -> handleToggleAutoFlip()
            is ChessUiEvent.ToggleThreatsClicked -> handleToggleThreats()
            is ChessUiEvent.ChangeGameMode -> handleChangeGameMode(event.mode)
            is ChessUiEvent.ChangeBotDifficulty -> handleChangeBotDifficulty(event.difficulty)
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
        if (currentState.gameStatus.isGameOver || currentState.isBotThinking) return

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
                    // Verificar si es promoción de peón
                    if (bridge.isPromotion(selected, position)) {
                        _uiState.update { it.copy(pendingPromotion = Pair(selected, position)) }
                    } else {
                        executeMove(from = selected, to = position)
                    }
                } else {
                    // Click en casilla no permitida: deseleccionar
                    clearSelection()
                }
            }
        }
    }

    private fun handlePromotionSelected(pieceType: PieceType) {
        val pending = _uiState.value.pendingPromotion ?: return
        executeMove(
            from = pending.first,
            to = pending.second,
            promotionPieceType = pieceType
        )
    }

    private fun handlePromotionDismissed() {
        clearSelection()
        _uiState.update { it.copy(pendingPromotion = null) }
    }

    private fun executeMove(
        from: Position,
        to: Position,
        promotionPieceType: PieceType = PieceType.QUEEN
    ) {
        val movingPiece = bridge.getPieceAt(from) ?: return
        val captured = bridge.getPieceAt(to)
        val movingColor = bridge.sideToMove
        val isPromo = bridge.isPromotion(from, to)
        val result = bridge.makeMove(from, to, promotionPieceType)

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

            val moveNotation = MoveNotationHelper.formatMove(
                piece = movingPiece,
                from = from,
                to = to,
                isCapture = captured != null,
                isCheck = newGameStatus == GameStatus.CHECK,
                isCheckmate = newGameStatus == GameStatus.CHECKMATE,
                promotionPiece = if (isPromo) promotionPieceType else null
            )

            val updatedHistory = updateMoveHistory(
                _uiState.value.moveHistory,
                movingColor,
                moveNotation
            )

            _uiState.update { state ->
                state.copy(
                    squares = bridge.getAllSquares(showThreats = state.showThreats),
                    selectedPosition = null,
                    pendingPromotion = null,
                    sideToMove = newSideToMove,
                    gameStatus = newGameStatus,
                    canUndo = bridge.canUndo,
                    statusMessage = getStatusText(newGameStatus, newSideToMove),
                    capturedByWhite = newCapturedWhite,
                    capturedByBlack = newCapturedBlack,
                    whiteMaterialAdvantage = whiteAdv,
                    blackMaterialAdvantage = blackAdv,
                    moveHistory = updatedHistory,
                    lastMoveFrom = from,
                    lastMoveTo = to,
                    isFlipped = newIsFlipped
                )
            }

            // Si estamos jugando contra el bot y es turno de Negras, se lanza el cálculo
            if (_uiState.value.gameMode == GameMode.VS_BOT &&
                newSideToMove == PieceColor.BLACK &&
                !newGameStatus.isGameOver
            ) {
                triggerBotMove()
            }
        } else {
            clearSelection()
        }
    }

    private fun updateMoveHistory(
        current: List<MoveLogItem>,
        color: PieceColor,
        notation: String
    ): List<MoveLogItem> {
        val list = current.toMutableList()
        if (color == PieceColor.WHITE) {
            list.add(MoveLogItem(moveNumber = list.size + 1, whiteMove = notation))
        } else {
            if (list.isNotEmpty()) {
                val last = list.removeAt(list.size - 1)
                list.add(last.copy(blackMove = notation))
            } else {
                list.add(MoveLogItem(moveNumber = 1, whiteMove = "-", blackMove = notation))
            }
        }
        return list
    }

    private fun rollbackMoveHistory(current: List<MoveLogItem>, count: Int): List<MoveLogItem> {
        val list = current.toMutableList()
        repeat(count) {
            if (list.isNotEmpty()) {
                val last = list.last()
                if (last.blackMove != null) {
                    list[list.size - 1] = last.copy(blackMove = null)
                } else {
                    list.removeAt(list.size - 1)
                }
            }
        }
        return list
    }

    private fun triggerBotMove() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBotThinking = true,
                    statusMessage = "El dispositivo está pensando..."
                )
            }

            // Retardo pedagógico para una respuesta humana y natural
            delay(450)

            val difficulty = _uiState.value.botDifficulty
            val botMove = withContext(botDispatcher) {
                bridge.findBestBotMove(difficulty)
            }

            _uiState.update { it.copy(isBotThinking = false) }

            if (botMove != null &&
                _uiState.value.gameMode == GameMode.VS_BOT &&
                !_uiState.value.gameStatus.isGameOver
            ) {
                executeMove(botMove.first, botMove.second)
            }
        }
    }

    private fun handleUndo() {
        if (!bridge.canUndo || _uiState.value.isBotThinking) return

        val movesToUndoCount = if (_uiState.value.gameMode == GameMode.VS_BOT && bridge.sideToMove == PieceColor.WHITE) {
            bridge.undoMove()
            bridge.undoMove()
            2
        } else {
            bridge.undoMove()
            1
        }

        val status = bridge.getGameStatus()
        val side = bridge.sideToMove
        val (whiteAdv, blackAdv) = bridge.getMaterialAdvantage()
        val shouldAutoFlip = _uiState.value.isAutoFlipEnabled &&
                _uiState.value.gameMode == GameMode.PASS_AND_PLAY

        val updatedHistory = rollbackMoveHistory(_uiState.value.moveHistory, movesToUndoCount)

        _uiState.update { state ->
            state.copy(
                squares = bridge.getAllSquares(showThreats = state.showThreats),
                selectedPosition = null,
                pendingPromotion = null,
                sideToMove = side,
                gameStatus = status,
                canUndo = bridge.canUndo,
                statusMessage = getStatusText(status, side),
                whiteMaterialAdvantage = whiteAdv,
                blackMaterialAdvantage = blackAdv,
                moveHistory = updatedHistory,
                lastMoveFrom = null,
                lastMoveTo = null,
                isFlipped = if (shouldAutoFlip) side == PieceColor.BLACK else state.isFlipped
            )
        }
    }

    private fun handleReset() {
        bridge.reset()
        _uiState.value = createInitialState().copy(
            gameMode = _uiState.value.gameMode,
            botDifficulty = _uiState.value.botDifficulty,
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
        _uiState.update {
            it.copy(
                gameMode = mode,
                isFlipped = if (mode == GameMode.VS_BOT) false else it.isFlipped,
                isAutoFlipEnabled = if (mode == GameMode.VS_BOT) false else it.isAutoFlipEnabled
            )
        }
    }

    private fun handleChangeBotDifficulty(difficulty: BotDifficulty) {
        _uiState.update { it.copy(botDifficulty = difficulty) }
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
