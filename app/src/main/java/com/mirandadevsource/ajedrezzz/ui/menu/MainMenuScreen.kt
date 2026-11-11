package com.mirandadevsource.ajedrezzz.ui.menu

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirandadevsource.ajedrezzz.R
import com.mirandadevsource.ajedrezzz.domain.ai.BotDifficulty
import com.mirandadevsource.ajedrezzz.ui.game.ChessUiEvent
import com.mirandadevsource.ajedrezzz.ui.game.ChessUiState
import com.mirandadevsource.ajedrezzz.ui.game.GameMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    state: ChessUiState,
    onEvent: (ChessUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDifficulty by remember { mutableStateOf(BotDifficulty.EASY) }
    var pendingNewGame by remember { mutableStateOf<Pair<GameMode, BotDifficulty>?>(null) }
    val scrollState = rememberScrollState()

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                // Fila de piezas decorativas en cabecera
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_piece_wk),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_piece_wq),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_piece_wn),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Ajedrezzz",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Aprende, practica y juega a tu ritmo",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // Opción 1: Reanudar partida suspendida (si existe)
                if (state.hasSuspendedGame) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Guardada",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Text(
                                    text = "Partida en Proceso",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                text = "Tienes una partida suspendida que puedes retomar exactamente donde la dejaste.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                            Button(
                                onClick = { onEvent(ChessUiEvent.ResumeSuspendedGame) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Continuar Partida")
                            }
                        }
                    }
                }

                // Opción 2: Jugar contra la Máquina
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Jugar contra la Máquina",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Entrena contra el oponente virtual. Puedes activar el Modo Tutor para ver amenazas y casillas seguras en tiempo real.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Selector de dificultad
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nivel:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            FilterChip(
                                selected = selectedDifficulty == BotDifficulty.EASY,
                                onClick = { selectedDifficulty = BotDifficulty.EASY },
                                label = { Text("Fácil") }
                            )
                            FilterChip(
                                selected = selectedDifficulty == BotDifficulty.MEDIUM,
                                onClick = { selectedDifficulty = BotDifficulty.MEDIUM },
                                label = { Text("Medio") }
                            )
                        }

                        Button(
                            onClick = {
                                if (state.hasSuspendedGame) {
                                    pendingNewGame = Pair(GameMode.VS_BOT, selectedDifficulty)
                                } else {
                                    onEvent(
                                        ChessUiEvent.StartNewGame(
                                            mode = GameMode.VS_BOT,
                                            difficulty = selectedDifficulty
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Iniciar vs Máquina")
                        }
                    }
                }

                // Opción 3: Partida de Dos Jugadores
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Partida de 2 Jugadores",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Juega en el mismo dispositivo por turnos. Incluye auto-giro de tablero para jugar cara a cara.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                if (state.hasSuspendedGame) {
                                    pendingNewGame = Pair(GameMode.PASS_AND_PLAY, BotDifficulty.EASY)
                                } else {
                                    onEvent(
                                        ChessUiEvent.StartNewGame(mode = GameMode.PASS_AND_PLAY)
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Iniciar 2 Jugadores")
                        }
                    }
                }
            }

            // Pie de página pedagógico
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "100% Offline • Sin Anuncios • Modo Tutor Pedagógico",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Diálogo de Advertencia si ya existe una partida suspendida
    if (pendingNewGame != null) {
        AlertDialog(
            onDismissRequest = { pendingNewGame = null },
            title = {
                Text(
                    text = "¿Iniciar nueva partida?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Tienes una partida guardada en proceso. Si comienzas una nueva partida, se perderá el progreso de la partida anterior.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val (mode, diff) = pendingNewGame!!
                        pendingNewGame = null
                        onEvent(ChessUiEvent.StartNewGame(mode = mode, difficulty = diff))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Iniciar de todos modos")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingNewGame = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
