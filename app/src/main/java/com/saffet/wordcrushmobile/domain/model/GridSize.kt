package com.saffet.wordcrushmobile.domain.model

/**
 * Yeni oyun için seçilebilecek tahta boyutları.
 */
enum class GridSize(
    val label: String,
    val description: String,
    val rows: Int,
    val cols: Int
) {
    SIX_BY_SIX("6x6 Grid", "Zor Seviye", 6, 6),
    EIGHT_BY_EIGHT("8x8 Grid", "Orta Seviye", 8, 8),
    TEN_BY_TEN("10x10 Grid", "Kolay Seviye", 10, 10);

    companion object {
        val DEFAULT: GridSize = EIGHT_BY_EIGHT
    }
}
