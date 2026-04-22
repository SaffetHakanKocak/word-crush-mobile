package com.saffet.wordcrushmobile.domain.model

/**
 * Şartname §Skor Tablosu'ndaki özet alanı için tek kaynaklı veri.
 *
 * Tüm alanlar Kotlin primitive'i + `String?`; Room bağımlılığı yoktur.
 * Repository katmanı bunu Flow olarak yayar, ViewModel doğrudan tüketir.
 *
 * @property totalGames           Oynanan oyun sayısı.
 * @property highScore            Tüm zamanların en yüksek puanı.
 * @property avgScore             Ortalama puan (tam sayıya yuvarlanmış).
 * @property totalWords           Bulunan toplam kelime.
 * @property longestWord          Tüm oyunlardaki en uzun kelime
 *                                (kaynak yoksa `null`).
 * @property totalDurationSeconds Toplam oyun süresi, saniye.
 */
data class GameStats(
    val totalGames: Int,
    val highScore: Int,
    val avgScore: Int,
    val totalWords: Int,
    val longestWord: String?,
    val totalDurationSeconds: Long
) {
    companion object {
        /** Hiç oyun oynanmamış durum için kullanılan boş başlangıç değeri. */
        val EMPTY = GameStats(
            totalGames = 0,
            highScore = 0,
            avgScore = 0,
            totalWords = 0,
            longestWord = null,
            totalDurationSeconds = 0L
        )
    }
}
