package com.saffet.wordcrushmobile.domain.model

/**
 * Yeni oyun için seçilebilecek hamle sayısı seçenekleri.
 */
enum class MoveOption(
    val description: String,
    val moves: Int
) {
    EASY("Kolay", 25),
    MEDIUM("Orta", 20),
    HARD("Zor", 15);

    companion object {
        val DEFAULT: MoveOption = MEDIUM
    }
}
