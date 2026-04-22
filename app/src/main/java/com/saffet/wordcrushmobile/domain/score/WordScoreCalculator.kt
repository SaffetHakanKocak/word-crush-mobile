package com.saffet.wordcrushmobile.domain.score

/**
 * Kelime puanını hesaplayan saf Kotlin servisi.
 *
 * Sorumluluk tek: verilen kelimeyi [LetterScores] ve opsiyonel modifier'lar
 * ile bir [WordScore] kırılımına çevirir.
 *
 * Genişletilebilirlik:
 *  - [letterScores] enjekte edilebilir → farklı diller / test double'ları.
 *  - [calculate] ikinci parametresi [ScoreModifiers] — combo çarpanı ve özel
 *    bonus'u taşır. İleride yeni bir modifier türü eklenirse (ör. süre
 *    bonusu), `ScoreModifiers` genişletilir ve buradaki formül güncellenir;
 *    çağıran tarafta kırılma olmaz.
 */
class WordScoreCalculator(
    private val letterScores: LetterScores = TurkishLetterScores
) {

    /**
     * Tek-kelime skorunu döndürür.
     *
     * @param word       Normalize edilmiş veya ham kelime (büyük/küçük fark etmez;
     *                   [LetterScores.scoreOf] Türkçe lowercase uygular).
     * @param modifiers  Combo çarpanı ve özel bonus. Varsayılan: etkisiz.
     */
    fun calculate(
        word: String,
        modifiers: ScoreModifiers = ScoreModifiers.NONE
    ): WordScore {
        if (word.isEmpty()) return WordScore.ZERO
        val base = word.sumOf { letterScores.scoreOf(it) }
        return WordScore(
            base = base,
            specialBonus = modifiers.specialBonus,
            comboMultiplier = modifiers.comboMultiplier
        )
    }
}

/**
 * Puan hesaplamasını etkileyen dışsal faktörlerin paketlenmiş hâli.
 *
 * Bu yapı, yeni modifier türleri eklendiğinde [WordScoreCalculator.calculate]
 * imzasını bozmadan genişletmemize olanak verir.
 *
 * @property comboMultiplier Ardışık doğru kelime serisinden gelen çarpan.
 *                           Örn: 1.0 / 1.25 / 1.5 / 2.0.
 * @property specialBonus    Özel hücre/güç tetiklemeleriyle gelen sabit ek puan.
 */
data class ScoreModifiers(
    val comboMultiplier: Float = 1f,
    val specialBonus: Int = 0
) {
    companion object {
        /** Hiçbir modifier uygulanmayan varsayılan. */
        val NONE = ScoreModifiers()
    }
}

/**
 * Ardışık doğru kelime serisini izleyen basit combo durumu.
 *
 * `GameViewModel` her başarılı kelime sonrası [increment], başarısız kelime
 * sonrası [reset] çağırarak `streak` değerini yönetir; `multiplier` alanı
 * doğrudan [ScoreModifiers] içine beslenebilir.
 *
 * İlk sürümde combo mantığı henüz aktif değildir; fakat altyapı hazır olduğu
 * için gelecekte yalnızca ViewModel entegrasyonu gerekecektir.
 */
data class ComboState(
    val streak: Int = 0
) {
    /**
     * Streak uzunluğundan çarpan üretir. Değerler dengelenmiş örneklerdir;
     * şartname/oyun dengesine göre güncellenebilir.
     */
    val multiplier: Float
        get() = when {
            streak >= 5 -> 2.0f
            streak >= 3 -> 1.5f
            streak >= 2 -> 1.25f
            else        -> 1.0f
        }

    fun increment(): ComboState = copy(streak = streak + 1)

    fun reset(): ComboState = copy(streak = 0)

    companion object {
        val NONE = ComboState()
    }
}
