package com.mirandadevsource.ajedrezzz.domain.model

enum class GameStatus {
    IN_PROGRESS,
    CHECK,
    CHECKMATE,
    STALEMATE,
    DRAW_INSUFFICIENT_MATERIAL,
    DRAW_REPETITION,
    DRAW_50_MOVES;

    val isGameOver: Boolean
        get() = this != IN_PROGRESS && this != CHECK
}
