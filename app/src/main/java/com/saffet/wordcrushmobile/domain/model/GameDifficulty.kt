package com.saffet.wordcrushmobile.domain.model

/**
 * Oyunun başlatılabileceği zorluk seviyeleri.
 *
 * Her seviye kendi tahta boyutunu ve oyuncuya verilen hamle sayısını taşır.
 * UI tarafında liste olarak [entries] üzerinden dolaşılarak seçim kartları oluşturulur.
 */
enum class GameDifficulty(
    val label: String,
    val description: String,
    val rows: Int,
    val cols: Int,
    val moves: Int
) {
    HARD(
        label = "6x6",
        description = "Zor",
        rows = 6,
        cols = 6,
        moves = 15
    ),
    MEDIUM(
        label = "8x8",
        description = "Orta",
        rows = 8,
        cols = 8,
        moves = 20
    ),
    EASY(
        label = "10x10",
        description = "Kolay",
        rows = 10,
        cols = 10,
        moves = 25
    );

    companion object {
        val DEFAULT: GameDifficulty = MEDIUM
    }
}
