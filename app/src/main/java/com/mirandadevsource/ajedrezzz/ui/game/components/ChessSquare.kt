package com.mirandadevsource.ajedrezzz.ui.game.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirandadevsource.ajedrezzz.domain.model.SquareUiModel

@Composable
fun ChessSquare(
    square: SquareUiModel,
    isLastMove: Boolean,
    showRankLabel: Boolean,
    showFileLabel: Boolean,
    rankLabel: String,
    fileLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseColor = if (square.isLightSquare) ChessColors.LightSquare else ChessColors.DarkSquare
    val labelColor = if (square.isLightSquare) ChessColors.CoordinateTextDark else ChessColors.CoordinateTextLight

    val backgroundColor = when {
        square.isCheck -> ChessColors.CheckHighlight
        square.isThreatened -> ChessColors.ThreatenedHighlight
        square.isHintSource || square.isHintDestination -> ChessColors.HintHighlight
        square.isSelected -> ChessColors.SelectedSquare
        isLastMove -> ChessColors.LastMoveHighlight
        else -> baseColor
    }

    val squareBorderModifier = when {
        square.isCheck -> Modifier.border(2.dp, ChessColors.CheckBorder)
        square.isThreatened -> Modifier.border(2.dp, ChessColors.ThreatenedBorder)
        square.isHintSource || square.isHintDestination -> Modifier.border(2.5.dp, ChessColors.HintBorder)
        else -> Modifier
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .then(squareBorderModifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Coordenada de Fila (número, ej. '8', '7')
        if (showRankLabel) {
            Text(
                text = rankLabel,
                color = labelColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 2.dp, top = 1.dp)
            )
        }

        // Coordenada de Columna (letra, ej. 'a', 'b')
        if (showFileLabel) {
            Text(
                text = fileLabel,
                color = labelColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 2.dp, bottom = 1.dp)
            )
        }

        // Pieza
        val piece = square.piece
        if (piece != null) {
            Image(
                painter = painterResource(id = piece.drawableRes),
                contentDescription = "${piece.color} ${piece.type}",
                modifier = Modifier.fillMaxSize(0.86f)
            )
        }

        // Indicador pedagógico: Casilla de captura legal (anillo exterior)
        if (square.isCaptureDestination) {
            val ringColor = when {
                square.isDangerousDestination -> ChessColors.BlunderWarningRing
                square.isSafeDestination -> ChessColors.SafeCaptureRing
                else -> ChessColors.LegalCaptureRing
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 4.dp,
                        color = ringColor,
                        shape = CircleShape
                    )
            )
        }

        // Indicador pedagógico: Casilla de movimiento legal pacífico (punto central)
        if (square.isLegalDestination) {
            val dotColor = when {
                square.isDangerousDestination -> ChessColors.BlunderWarningDot
                square.isSafeDestination -> ChessColors.SafeMoveDot
                else -> ChessColors.LegalMoveDot
            }
            Box(
                modifier = Modifier
                    .size(if (square.isDangerousDestination || square.isSafeDestination) 15.dp else 14.dp)
                    .background(color = dotColor, shape = CircleShape)
            )
        }
    }
}
