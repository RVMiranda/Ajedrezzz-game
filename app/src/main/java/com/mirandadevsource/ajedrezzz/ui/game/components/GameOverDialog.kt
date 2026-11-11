package com.mirandadevsource.ajedrezzz.ui.game.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirandadevsource.ajedrezzz.ui.game.ChessUiState
import com.mirandadevsource.ajedrezzz.ui.game.GameMode
import com.mirandadevsource.ajedrezzz.ui.game.GameOutcome

private data class Particle(
    val angle: Float,
    val speed: Float,
    val color: Color,
    val radius: Float,
    val isStar: Boolean
)

@Composable
fun CelebrationParticles(
    modifier: Modifier = Modifier,
    particleCount: Int = 32
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val particles = remember {
        List(particleCount) { i ->
            val angle = (i * (360f / particleCount) + (i % 3) * 12f) * (Math.PI.toFloat() / 180f)
            val speed = 0.55f + (i % 4) * 0.15f
            val color = when (i % 4) {
                0 -> Color(0xFFFFD54F) // Oro / Amarillo
                1 -> Color(0xFFFF8A65) // Coral cálido
                2 -> Color(0xFF81C784) // Verde suave
                else -> Color(0xFF64B5F6) // Azul cielo
            }
            val radius = 3f + (i % 3) * 1.5f
            val isStar = i % 2 == 0
            Particle(angle, speed, color, radius, isStar)
        }
    }

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val maxDist = minOf(size.width, size.height) * 0.48f

        particles.forEach { p ->
            val distance = progress * maxDist * p.speed
            val alpha = (1f - progress).coerceIn(0f, 1f)
            val x = centerX + kotlin.math.cos(p.angle) * distance
            val y = centerY + kotlin.math.sin(p.angle) * distance

            if (p.isStar) {
                val starSize = p.radius * (1f - progress * 0.25f)
                drawLine(
                    color = p.color.copy(alpha = alpha),
                    start = Offset(x - starSize * 2, y),
                    end = Offset(x + starSize * 2, y),
                    strokeWidth = 2.5f
                )
                drawLine(
                    color = p.color.copy(alpha = alpha),
                    start = Offset(x, y - starSize * 2),
                    end = Offset(x, y + starSize * 2),
                    strokeWidth = 2.5f
                )
            } else {
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.radius * (1f - progress * 0.35f),
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
fun GameOverDialog(
    state: ChessUiState,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    val outcome = state.getOutcome()
    val isVictory = outcome == GameOutcome.VICTORY ||
            outcome == GameOutcome.VICTORY_WHITE ||
            outcome == GameOutcome.VICTORY_BLACK

    val headerTitle = when (outcome) {
        GameOutcome.VICTORY -> "¡Victoria! ¡Felicidades!"
        GameOutcome.DEFEAT -> "¡Buen intento!"
        GameOutcome.DRAW -> "¡Partida en Tablas!"
        GameOutcome.VICTORY_WHITE -> "¡Victoria de las Blancas!"
        GameOutcome.VICTORY_BLACK -> "¡Victoria de las Negras!"
    }

    val motivationalMessage = when (outcome) {
        GameOutcome.VICTORY -> "Excelente visión táctica. Has jugado con calma y dominado el tablero."
        GameOutcome.DEFEAT -> "En el ajedrez cada partida te enseña algo nuevo para la siguiente. Analiza tus piezas y ¡vuelve a intentarlo con ánimo!"
        GameOutcome.DRAW -> "Gran equilibrio en el tablero. Ambos bandos lucharon con destreza y empataron la partida."
        GameOutcome.VICTORY_WHITE -> "Las piezas blancas lograron el jaque mate definitivo con una gran estrategia."
        GameOutcome.VICTORY_BLACK -> "Las piezas negras lograron el jaque mate definitivo con una gran estrategia."
    }

    AlertDialog(
        onDismissRequest = { },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isVictory) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CelebrationParticles(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "★",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(bottom = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (outcome == GameOutcome.DRAW) "⚖" else "✦",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = headerTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = if (isVictory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = motivationalMessage,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Tarjeta de Resumen Estadístico
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Resumen de la Partida",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        StatRow(
                            label = "Movimientos totales:",
                            value = "${state.totalTurnsCount} turnos"
                        )

                        if (state.gameMode == GameMode.VS_BOT) {
                            StatRow(
                                label = "Piezas comidas (Tú):",
                                value = "${state.capturedByWhite.size}"
                            )
                            StatRow(
                                label = "Piezas perdidas (Tú):",
                                value = "${state.capturedByBlack.size}"
                            )
                            StatRow(
                                label = "Jaques ejecutados:",
                                value = "${state.whiteChecksCount}"
                            )
                            val balanceText = when {
                                state.whiteMaterialAdvantage > 0 -> "+${state.whiteMaterialAdvantage} a favor"
                                state.blackMaterialAdvantage > 0 -> "-${state.blackMaterialAdvantage} en contra"
                                else -> "Igualado"
                            }
                            StatRow(label = "Balance de material:", value = balanceText)
                        } else {
                            StatRow(
                                label = "Piezas comidas (Blancas):",
                                value = "${state.capturedByWhite.size}"
                            )
                            StatRow(
                                label = "Piezas comidas (Negras):",
                                value = "${state.capturedByBlack.size}"
                            )
                            StatRow(
                                label = "Jaques de Blancas:",
                                value = "${state.whiteChecksCount}"
                            )
                            StatRow(
                                label = "Jaques de Negras:",
                                value = "${state.blackChecksCount}"
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onPlayAgain,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Jugar otra vez")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onMainMenu,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Menú Principal")
            }
        }
    )
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
