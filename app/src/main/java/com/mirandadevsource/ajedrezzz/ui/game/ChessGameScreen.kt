package com.mirandadevsource.ajedrezzz.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mirandadevsource.ajedrezzz.domain.model.GameStatus
import com.mirandadevsource.ajedrezzz.domain.model.PieceColor
import com.mirandadevsource.ajedrezzz.ui.game.components.CapturedPiecesBar
import com.mirandadevsource.ajedrezzz.ui.game.components.ChessBoard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessGameScreen(
    viewModel: ChessViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Ajedrezzz",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "2 Jugadores",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
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
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (state.gameStatus) {
                        GameStatus.CHECK, GameStatus.CHECKMATE -> MaterialTheme.colorScheme.errorContainer
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
                    // Círculo indicador del bando
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (state.sideToMove == PieceColor.WHITE) Color.White else Color(0xFF222222))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.statusMessage,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = when (state.gameStatus) {
                            GameStatus.CHECK, GameStatus.CHECKMATE -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    )
                }
            }

            // 2. Bandeja de Piezas Capturadas Superior (del bando opuesto según perspectiva)
            val topCaptures = if (state.isFlipped) state.capturedByBlack else state.capturedByWhite
            val topLabel = if (state.isFlipped) "Capturas Negras:" else "Capturas Blancas:"
            CapturedPiecesBar(
                pieces = topCaptures,
                label = topLabel,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // 3. Tablero 8x8 Central
            ChessBoard(
                state = state,
                onSquareClick = { pos -> viewModel.onEvent(ChessUiEvent.SquareClicked(pos)) },
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // 4. Bandeja de Piezas Capturadas Inferior
            val bottomCaptures = if (state.isFlipped) state.capturedByWhite else state.capturedByBlack
            val bottomLabel = if (state.isFlipped) "Capturas Blancas:" else "Capturas Negras:"
            CapturedPiecesBar(
                pieces = bottomCaptures,
                label = bottomLabel,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // 5. Barra de Controles Inferiores (Deshacer, Girar, Auto-Giro, Reiniciar)
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
                        enabled = state.canUndo && !state.gameStatus.isGameOver,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Deshacer")
                    }

                    OutlinedButton(
                        onClick = { viewModel.onEvent(ChessUiEvent.ToggleFlipClicked) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Girar")
                    }
                }

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
                        Text("Reiniciar Partida")
                    }
                }
            }
        }
    }

    // Diálogo de Confirmación de Reinicio
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

    // Diálogo de Fin de Partida
    if (state.gameStatus.isGameOver) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("¡Fin de la Partida!") },
            text = { Text(state.statusMessage) },
            confirmButton = {
                Button(onClick = { viewModel.onEvent(ChessUiEvent.ResetGameClicked) }) {
                    Text("Jugar otra vez")
                }
            }
        )
    }
}
