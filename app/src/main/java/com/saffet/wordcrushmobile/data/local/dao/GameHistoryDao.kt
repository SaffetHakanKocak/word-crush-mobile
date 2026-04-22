package com.saffet.wordcrushmobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.saffet.wordcrushmobile.data.local.entity.GameHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * `game_history` tablosu için DAO.
 *
 * Yazma işlemleri `suspend`; okumalar `Flow<...>` döner. Böylece Room,
 * UI'nın tablo güncellendiğinde otomatik olarak yeni veriyi almasını
 * sağlar (`Skor Tablosu` ekranı için gerekli).
 */
@Dao
interface GameHistoryDao {

    @Insert
    suspend fun insert(entity: GameHistoryEntity): Long

    /** En yeni oyun en üstte olacak şekilde tüm kayıtlar. */
    @Query("SELECT * FROM game_history ORDER BY played_at DESC")
    fun observeAll(): Flow<List<GameHistoryEntity>>

    @Query("SELECT * FROM game_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): GameHistoryEntity?

    /**
     * Şartname §Skor Tablosu'nda istenen özet bilgilerinden
     * string olmayanlar: tek sorguda agregat döner.
     * En uzun kelime ayrı sorguyla çekilir (SQLite'ta string uzunluğu
     * hesabına hizalı ve taşınabilir kalması için).
     */
    @Query(
        """
        SELECT 
            COUNT(*) AS totalGames,
            COALESCE(MAX(score), 0) AS highScore,
            COALESCE(CAST(AVG(score) AS INTEGER), 0) AS avgScore,
            COALESCE(SUM(word_count), 0) AS totalWords,
            COALESCE(SUM(duration_seconds), 0) AS totalDurationSeconds
        FROM game_history
        """
    )
    fun observeAggregate(): Flow<GameAggregate>

    /**
     * Tüm oyunlardaki "en uzun kelime" alanlarının listesi.
     * Repository, Kotlin tarafında en uzununu seçer — böylece SQLite'ın
     * `LENGTH()` fonksiyonunun yerelliğine bağımlı olmayız.
     */
    @Query("SELECT longest_word FROM game_history WHERE longest_word != ''")
    fun observeLongestCandidates(): Flow<List<String>>

}

/**
 * [GameHistoryDao.observeAggregate] tarafından üretilen satır.
 * İçerik data sınıfı alan adları SELECT alias'larıyla birebir eşleşir.
 */
data class GameAggregate(
    val totalGames: Int,
    val highScore: Int,
    val avgScore: Int,
    val totalWords: Int,
    val totalDurationSeconds: Long
)
