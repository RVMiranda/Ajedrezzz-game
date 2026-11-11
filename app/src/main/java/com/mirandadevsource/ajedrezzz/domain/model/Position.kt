package com.mirandadevsource.ajedrezzz.domain.model

/**
 * Representa una coordenada en el tablero de ajedrez.
 * @param file Columna del 0 al 7 (0 = 'a', 7 = 'h')
 * @param rank Fila del 0 al 7 (0 = '1', 7 = '8')
 */
data class Position(val file: Int, val rank: Int) {
    init {
        require(file in 0..7 && rank in 0..7) {
            "Coordenadas fuera del tablero de ajedrez: file=$file, rank=$rank"
        }
    }

    val algebraic: String
        get() = "${('a' + file)}${rank + 1}"

    companion object {
        fun fromAlgebraic(notation: String): Position {
            require(notation.length == 2) { "Notación algebraica inválida: $notation" }
            val f = notation.lowercase()[0] - 'a'
            val r = notation[1] - '1'
            return Position(f, r)
        }
    }
}
