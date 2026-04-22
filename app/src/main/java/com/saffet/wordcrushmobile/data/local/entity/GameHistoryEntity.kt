package com.saffet.wordcrushmobile.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tek bir oyun oturumunun geçmiş kaydı. "Skor Tablosu" ekranının veri
 * kaynağıdır.
 *
 * Entity yalnızca data katmanında görünür; repository'ler bunu domain
 * modeline (`GameRecord`) çevirerek dışa açar.
 *
 * Tarih UTC-epoch ms olarak saklanır (UI katmanında formatlanır).
 * Süre saniye birimindedir (kısa oyunlar için dk hassasiyeti yetersiz).
 */
@Entity(tableName = "game_history")
data class GameHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Oyunun oynandığı an (epoch millis, UTC). */
    @ColumnInfo(name = "played_at")
    val playedAt: Long,

    /** Grid satır sayısı (6 / 8 / 10). */
    val rows: Int,

    /** Grid sütun sayısı (6 / 8 / 10). */
    val cols: Int,

    /** Başlangıçta verilen toplam hamle sayısı. */
    @ColumnInfo(name = "total_moves")
    val totalMoves: Int,

    /** Kullanıcının harcadığı hamle sayısı. */
    @ColumnInfo(name = "moves_used")
    val movesUsed: Int,

    /** Oyun sonu toplam skor. */
    val score: Int,

    /** Bulunan geçerli kelime sayısı. */
    @ColumnInfo(name = "word_count")
    val wordCount: Int,

    /** Bu oyundaki en uzun kelime ("" ise hiç bulunamadı). */
    @ColumnInfo(name = "longest_word")
    val longestWord: String,

    /** Oyunun süresi (saniye). */
    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Long,

    /** Oyun oynamadan/yarıda bırakılarak mı sonlandırıldı? */
    val abandoned: Boolean
)
