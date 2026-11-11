package com.mirandadevsource.ajedrezzz.ui.game.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mirandadevsource.ajedrezzz.domain.model.Position
import com.mirandadevsource.ajedrezzz.ui.game.ChessUiState

@Composable
fun ChessBoard(
    state: ChessUiState,
    onSquareClick: (Position) -> Unit,
    modifier: Modifier = Modifier
) {
    // Definimos el orden de filas y columnas según la orientación del tablero
    val rankRange = if (!state.isFlipped) 7 downTo 0 else 0..7
    val fileRange = if (!state.isFlipped) 0..7 else 7 downTo 0

    // Indexamos las casillas en un Map para acceso O(1) rápido durante la composición
    val squaresMap = state.squares.associateBy { it.position }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .border(2.dp, Color(0xFF4A6B35), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for ((rowIndex, rank) in rankRange.withIndex()) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    for ((colIndex, file) in fileRange.withIndex()) {
                        val pos = Position(file, rank)
                        val square = squaresMap[pos]

                        if (square != null) {
                            val isLastMove = pos == state.lastMoveFrom || pos == state.lastMoveTo
                            val showRankLabel = colIndex == 0
                            val showFileLabel = rowIndex == 7

                            ChessSquare(
                                square = square,
                                isLastMove = isLastMove,
                                showRankLabel = showRankLabel,
                                showFileLabel = showFileLabel,
                                rankLabel = "${rank + 1}",
                                fileLabel = "${'a' + file}",
                                onClick = { onSquareClick(pos) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                }
            }
        }
    }
}
