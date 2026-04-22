package com.saffet.wordcrushmobile.domain.model

/**
 * Bir oyun bittiğinde gösterilecek skor özetini temsil eder.
 *
 * Oyun içi [GameState] bilgisi özetlenerek bu nesneye dönüştürülür ve
 * sonuç ekranına / leaderboard'a aktarılır.
 *
 * @property totalScore     Oyuncunun oyun sonunda ulaştığı toplam puan.
 * @property wordsFound     Başarıyla oynanan kelime sayısı.
 * @property longestWord    Oyun boyunca bulunmuş en uzun kelime metni
 *                          (boş string olabilir: hiç kelime bulunmamış olabilir).
 * @property bestWordScore  Tek bir kelimeyle kazanılan en yüksek skor.
 * @property movesUsed      Toplamda kaç hamle kullanıldı.
 */
data class ScoreSummary(
    val totalScore: Int,
    val wordsFound: Int,
    val longestWord: String,
    val bestWordScore: Int,
    val movesUsed: Int
)
