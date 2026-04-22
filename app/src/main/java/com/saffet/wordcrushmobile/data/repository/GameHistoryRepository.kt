package com.saffet.wordcrushmobile.data.repository

import com.saffet.wordcrushmobile.data.local.dao.GameHistoryDao
import com.saffet.wordcrushmobile.data.local.entity.GameHistoryEntity
import com.saffet.wordcrushmobile.domain.model.GameRecord
import com.saffet.wordcrushmobile.domain.model.GameStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Oyun geçmişi için domain-cinsli repository.
 *
 * UI/ViewModel katmanı Room entity'lerini görmez; repository entity ↔ domain
 * dönüşümünü kendi içinde yapar.
 */
class GameHistoryRepository(
    private val dao: GameHistoryDao
) {

    /** En yeni oyun başta olacak şekilde tüm kayıtlar. */
    fun observeAll(): Flow<List<GameRecord>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    /**
     * Kullanıcının genel performansını özetleyen istatistik akışı.
     *
     * En uzun kelime SQLite `LENGTH()` yerine Kotlin tarafında hesaplanır;
     * bu sayede Türkçe karakterlerde de doğru uzunluk elde edilir.
     */
    fun observeStats(): Flow<GameStats> =
        combine(
            dao.observeAggregate(),
            dao.observeLongestCandidates()
        ) { agg, candidates ->
            GameStats(
                totalGames = agg.totalGames,
                highScore = agg.highScore,
                avgScore = agg.avgScore,
                totalWords = agg.totalWords,
                longestWord = candidates.maxByOrNull { it.length },
                totalDurationSeconds = agg.totalDurationSeconds
            )
        }

    suspend fun save(record: GameRecord): Long =
        dao.insert(record.toEntity())

    // --- Mappers -------------------------------------------------------

    private fun GameHistoryEntity.toDomain(): GameRecord = GameRecord(
        id = id,
        playedAt = playedAt,
        rows = rows,
        cols = cols,
        totalMoves = totalMoves,
        movesUsed = movesUsed,
        score = score,
        wordCount = wordCount,
        longestWord = longestWord,
        durationSeconds = durationSeconds,
        abandoned = abandoned
    )

    private fun GameRecord.toEntity(): GameHistoryEntity = GameHistoryEntity(
        id = id,
        playedAt = playedAt,
        rows = rows,
        cols = cols,
        totalMoves = totalMoves,
        movesUsed = movesUsed,
        score = score,
        wordCount = wordCount,
        longestWord = longestWord,
        durationSeconds = durationSeconds,
        abandoned = abandoned
    )
}
