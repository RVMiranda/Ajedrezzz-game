package com.mirandadevsource.ajedrezzz.domain.storage

import com.mirandadevsource.ajedrezzz.domain.ai.BotDifficulty
import com.mirandadevsource.ajedrezzz.domain.model.MoveLogItem
import com.mirandadevsource.ajedrezzz.ui.game.GameMode

/**
 * Representa la fotografía completa de una partida suspendida para su posterior reanudación.
 */
data class SavedGameState(
    val fen: String,
    val gameMode: GameMode,
    val botDifficulty: BotDifficulty,
    val moveHistory: List<MoveLogItem>,
    val isFlipped: Boolean,
    val isAutoFlipEnabled: Boolean,
    val showThreats: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
