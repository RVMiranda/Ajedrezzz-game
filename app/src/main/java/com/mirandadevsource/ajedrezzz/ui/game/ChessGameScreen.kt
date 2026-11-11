package com.mirandadevsource.ajedrezzz.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mirandadevsource.ajedrezzz.domain.model.GameStatus
import com.mirandadevsource.ajedrezzz.domain.model.PieceColor
import com.mirandadevsource.ajedrezzz.ui.game.components.CapturedPiecesBar
import com.mirandadevsource.ajedrezzz.ui.game.components.ChessBoard
import com.mirandadevsource.ajedrezzz.ui.game.components.GameOverDialog
import com.mirandadevsource.ajedrezzz.ui.game.components.MoveHistoryScoresheet
import com.mirandadevsource.ajedrezzz.ui.game.components.PauseDialog
import com.mirandadevsource.ajedrezzz.ui.game.components.PromotionDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessGameScreen(
    viewModel: ChessViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    var showResetDialog by remember { mutableStateOf(false) }

    // Interceptar botón atrás del sistema para abrir o cerrar el menú de pausa
    BackHandler {
        if (state.showPauseDialog) {
            viewModel.onEvent(ChessUiEvent.ResumeGameClicked)
        } else {
            viewModel.onEvent(ChessUiEvent.PauseClicked)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Ajedrezzz",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = when (state.gameMode) {
                                GameMode.VS_BOT -> "vs Dispositivo (${state.botDifficulty.title})"
                                GameMode.PASS_AND_PLAY -> "2 Jugadores (Local)"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    FilterChip(
                        selected = state.showThreats,
                        onClick = { viewModel.onEvent(ChessUiEvent.ToggleThreatsClicked) },
                        label = { Text("Tutor") },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    if (state.showThreats) {
                        FilledTonalButton(
                            onClick = { viewModel.onEvent(ChessUiEvent.RequestHintClicked) },
                            enabled = !state.isBotThinking && !state.gameStatus.isGameOver,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text("Pista", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    FilledTonalButton(
                        onClick = { viewModel.onEvent(ChessUiEvent.PauseClicked) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Pausa", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Tarjeta de Estado del Turno
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        state.gameStatus == GameStatus.CHECK || state.gameStatus == GameStatus.CHECKMATE -> MaterialTheme.colorScheme.errorContainer
                        state.isBotThinking -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (state.isBotThinking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        // Círculo indicador del bando
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (state.sideToMove == PieceColor.WHITE) Color.White else Color(0xFF222222))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = state.statusMessage,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = when {
                            state.gameStatus == GameStatus.CHECK || state.gameStatus == GameStatus.CHECKMATE -> MaterialTheme.colorScheme.onErrorContainer
                            state.isBotThinking -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    )
                }
            }

            // Banner o pista del Modo Tutor
            if (state.hintExplanation != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "💡 ${state.hintExplanation}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { viewModel.onEvent(ChessUiEvent.ClearHintClicked) },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text("Cerrar", fontSize = 11.sp)
                        }
                    }
                }
            } else if (state.showThreats) {
                Text(
                    text = "Tutor: verde = casilla segura • naranja = casilla atacada",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }

            // 2. Bandeja de Piezas Capturadas Superior
            val topCaptures = if (state.isFlipped) state.capturedByBlack else state.capturedByWhite
            val topLabel = if (state.isFlipped) "Capturas Negras:" else "Capturas Blancas:"
            val topAdvantage = if (state.isFlipped) state.blackMaterialAdvantage else state.whiteMaterialAdvantage
            CapturedPiecesBar(
                pieces = topCaptures,
                label = topLabel,
                materialAdvantage = topAdvantage,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // 3. Tablero 8x8 Central
            ChessBoard(
                state = state,
                onSquareClick = { pos ->
                    if (!state.isBotThinking) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onEvent(ChessUiEvent.SquareClicked(pos))
                    }
                },
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // 4. Bandeja de Piezas Capturadas Inferior
            val bottomCaptures = if (state.isFlipped) state.capturedByWhite else state.capturedByBlack
            val bottomLabel = if (state.isFlipped) "Capturas Blancas:" else "Capturas Negras:"
            val bottomAdvantage = if (state.isFlipped) state.whiteMaterialAdvantage else state.blackMaterialAdvantage
            CapturedPiecesBar(
                pieces = bottomCaptures,
                label = bottomLabel,
                materialAdvantage = bottomAdvantage,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // 5. Planilla Pedagógica de Jugadas en 2 Columnas (Blancas y Negras)
            MoveHistoryScoresheet(
                history = state.moveHistory,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // 6. Barra de Controles Inferiores (Deshacer, Girar, Auto-Giro)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedButton(
                        onClick = { viewModel.onEvent(ChessUiEvent.UndoClicked) },
                        enabled = state.canUndo && !state.gameStatus.isGameOver && !state.isBotThinking,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Deshacer")
                    }

                    if (state.gameMode == GameMode.PASS_AND_PLAY) {
                        OutlinedButton(
                            onClick = { viewModel.onEvent(ChessUiEvent.ToggleFlipClicked) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Girar")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reiniciar")
                        }
                    }
                }

                if (state.gameMode == GameMode.PASS_AND_PLAY) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = state.isAutoFlipEnabled,
                            onClick = { viewModel.onEvent(ChessUiEvent.ToggleAutoFlipClicked) },
                            label = { Text("Auto-Girar por turno") }
                        )

                        TextButton(onClick = { showResetDialog = true }) {
                            Text("Reiniciar")
                        }
                    }
                }
            }
        }
    }

    // Diálogo de Coronación / Promoción de Peón
    if (state.pendingPromotion != null) {
        PromotionDialog(
            color = state.sideToMove,
            onPieceSelected = { pieceType ->
                viewModel.onEvent(ChessUiEvent.PromotionSelected(pieceType))
            },
            onDismiss = {
                viewModel.onEvent(ChessUiEvent.PromotionDismissed)
            }
        )
    }

    // Diálogo de Menú de Pausa
    if (state.showPauseDialog) {
        PauseDialog(
            onResume = { viewModel.onEvent(ChessUiEvent.ResumeGameClicked) },
            onRestart = { viewModel.onEvent(ChessUiEvent.ResetGameClicked) },
            onSuspendAndExit = { viewModel.onEvent(ChessUiEvent.SuspendAndExitClicked) },
            onResign = { viewModel.onEvent(ChessUiEvent.ResignGameClicked) },
            onDismiss = { viewModel.onEvent(ChessUiEvent.ResumeGameClicked) }
        )
    }

    // Diálogo de Confirmación de Reinicio Rápido
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("¿Reiniciar partida?") },
            text = { Text("Se restablecerán todas las piezas a su posición inicial.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(ChessUiEvent.ResetGameClicked)
                        showResetDialog = false
                    }
                ) {
                    Text("Sí, reiniciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de Fin de Partida Enriquecido con Animación y Resumen Estadístico
    if (state.gameStatus.isGameOver) {
        GameOverDialog(
            state = state,
            onPlayAgain = { viewModel.onEvent(ChessUiEvent.ResetGameClicked) },
            onMainMenu = { viewModel.onEvent(ChessUiEvent.ReturnToMainMenuClicked) }
        )
    }
}
