package com.saffet.wordcrushmobile.domain.model

/**
 * UI ve ViewModel'in kullandığı, Room'dan bağımsız oyun kaydı modeli.
 *
 * Data katmanındaki [com.saffet.wordcrushmobile.data.local.entity.GameHistoryEntity]
 * ile neredeyse birebirdir; domain katmanını Room annotation'larından soyutlamak
 * için ayrıdır.
 */
data class GameRecord(
    val id: Long,
    val playedAt: Long,
    val rows: Int,
    val cols: Int,
    val totalMoves: Int,
    val movesUsed: Int,
    val score: Int,
    val wordCount: Int,
    val longestWord: String,
    val durationSeconds: Long,
    val abandoned: Boolean
)
