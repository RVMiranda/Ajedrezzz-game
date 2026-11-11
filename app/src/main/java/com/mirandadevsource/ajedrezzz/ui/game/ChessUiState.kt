package com.mirandadevsource.ajedrezzz.ui.game

import com.mirandadevsource.ajedrezzz.domain.ai.BotDifficulty
import com.mirandadevsource.ajedrezzz.domain.model.GameStatus
import com.mirandadevsource.ajedrezzz.domain.model.MoveLogItem
import com.mirandadevsource.ajedrezzz.domain.model.Piece
import com.mirandadevsource.ajedrezzz.domain.model.PieceColor
import com.mirandadevsource.ajedrezzz.domain.model.PieceType
import com.mirandadevsource.ajedrezzz.domain.model.Position
import com.mirandadevsource.ajedrezzz.domain.model.SquareUiModel

enum class AppScreen {
    MAIN_MENU,
    GAME
}

enum class GameMode {
    PASS_AND_PLAY, // 2 jugadores en el mismo dispositivo
    VS_BOT         // Contra el dispositivo
}

data class ChessUiState(
    val currentScreen: AppScreen = AppScreen.MAIN_MENU,
    val hasSuspendedGame: Boolean = false,
    val showPauseDialog: Boolean = false,
    val squares: List<SquareUiModel> = emptyList(),
    val selectedPosition: Position? = null,
    val sideToMove: PieceColor = PieceColor.WHITE,
    val gameStatus: GameStatus = GameStatus.IN_PROGRESS,
    val gameMode: GameMode = GameMode.PASS_AND_PLAY,
    val botDifficulty: BotDifficulty = BotDifficulty.EASY,
    val isBotThinking: Boolean = false,
    val isFlipped: Boolean = false,
    val isAutoFlipEnabled: Boolean = false,
    val canUndo: Boolean = false,
    val statusMessage: String = "Turno de las Blancas",
    val capturedByWhite: List<Piece> = emptyList(),
    val capturedByBlack: List<Piece> = emptyList(),
    val lastMoveFrom: Position? = null,
    val lastMoveTo: Position? = null,
    val showThreats: Boolean = false,
    val whiteMaterialAdvantage: Int = 0,
    val blackMaterialAdvantage: Int = 0,
    val pendingPromotion: Pair<Position, Position>? = null,
    val moveHistory: List<MoveLogItem> = emptyList()
)

sealed class ChessUiEvent {
    data class StartNewGame(val mode: GameMode, val difficulty: BotDifficulty = BotDifficulty.EASY) : ChessUiEvent()
    data object ResumeSuspendedGame : ChessUiEvent()
    data object PauseClicked : ChessUiEvent()
    data object ResumeGameClicked : ChessUiEvent()
    data object SuspendAndExitClicked : ChessUiEvent()
    data object ResignGameClicked : ChessUiEvent()
    data object ReturnToMainMenuClicked : ChessUiEvent()
    data class SquareClicked(val position: Position) : ChessUiEvent()
    data class PromotionSelected(val pieceType: PieceType) : ChessUiEvent()
    data object PromotionDismissed : ChessUiEvent()
    data object UndoClicked : ChessUiEvent()
    data object ResetGameClicked : ChessUiEvent()
    data object ToggleFlipClicked : ChessUiEvent()
    data object ToggleAutoFlipClicked : ChessUiEvent()
    data object ToggleThreatsClicked : ChessUiEvent()
    data class ChangeGameMode(val mode: GameMode) : ChessUiEvent()
    data class ChangeBotDifficulty(val difficulty: BotDifficulty) : ChessUiEvent()
}
