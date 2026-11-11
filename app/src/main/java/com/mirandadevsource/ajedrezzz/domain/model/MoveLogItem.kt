package com.mirandadevsource.ajedrezzz.domain.model

data class MoveLogItem(
    val moveNumber: Int,
    val whiteMove: String,
    val blackMove: String? = null
)
