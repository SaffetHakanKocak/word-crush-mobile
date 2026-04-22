package com.saffet.wordcrushmobile.domain.model

/**
 * UI ve iş kurallarında kullanılan saf domain modeli.
 * Veritabanı veya API şemasından bağımsızdır.
 */
data class GameModel(
    val id: Long = 0L,
    val score: Int = 0,
    val level: Int = 1,
    val board: List<List<Char>> = emptyList()
)
