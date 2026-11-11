package com.mirandadevsource.ajedrezzz.domain.model

import androidx.annotation.DrawableRes
import com.mirandadevsource.ajedrezzz.R

enum class PieceType(val materialValue: Int) {
    PAWN(1),
    KNIGHT(3),
    BISHOP(3),
    ROOK(5),
    QUEEN(9),
    KING(0)
}

enum class PieceColor {
    WHITE,
    BLACK;

    val opposite: PieceColor
        get() = if (this == WHITE) BLACK else WHITE
}

data class Piece(
    val type: PieceType,
    val color: PieceColor
) {
    @get:DrawableRes
    val drawableRes: Int
        get() = when (color) {
            PieceColor.WHITE -> when (type) {
                PieceType.PAWN -> R.drawable.ic_piece_wp
                PieceType.KNIGHT -> R.drawable.ic_piece_wn
                PieceType.BISHOP -> R.drawable.ic_piece_wb
                PieceType.ROOK -> R.drawable.ic_piece_wr
                PieceType.QUEEN -> R.drawable.ic_piece_wq
                PieceType.KING -> R.drawable.ic_piece_wk
            }
            PieceColor.BLACK -> when (type) {
                PieceType.PAWN -> R.drawable.ic_piece_bp
                PieceType.KNIGHT -> R.drawable.ic_piece_bn
                PieceType.BISHOP -> R.drawable.ic_piece_bb
                PieceType.ROOK -> R.drawable.ic_piece_br
                PieceType.QUEEN -> R.drawable.ic_piece_bq
                PieceType.KING -> R.drawable.ic_piece_bk
            }
        }
}
