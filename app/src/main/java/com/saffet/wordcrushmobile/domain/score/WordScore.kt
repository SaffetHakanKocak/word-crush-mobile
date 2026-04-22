package com.saffet.wordcrushmobile.domain.score

import kotlin.math.roundToInt

/**
 * Bir kelimenin skor hesabının kırılımı.
 *
 * `total` alanı diğer alanlardan üretilir:
 *   total = round((base + specialBonus) * comboMultiplier)
 *
 * Bu ayrıştırma sayesinde:
 *  - UI'da "Taban: X, Combo ×Y, Toplam Z" şeklinde detay gösterilebilir.
 *  - Testlerde her bileşen bağımsız doğrulanabilir.
 *  - İleride yeni skor bileşenleri eklemek (ör. süre bonusu) mevcut
 *    kullanıcıları kırmadan mümkündür.
 *
 * @property base             Harf puanları toplamı (şartname kuralı).
 * @property specialBonus     Özel hücre/güç bonusu (bomba çarpanı, satır
 *                            temizleme ödülü vs). İlk sürümde 0.
 * @property comboMultiplier  Ardışık doğru kelimeler için çarpan.
 *                            1.0 = combo yok. Kullanım ileride gelecek.
 */
data class WordScore(
    val base: Int,
    val specialBonus: Int = 0,
    val comboMultiplier: Float = 1f
) {
    /** Kullanıcıya eklenecek nihai puan. */
    val total: Int
        get() = ((base + specialBonus) * comboMultiplier).roundToInt()

    companion object {
        /** Boş/geçersiz durum için sıfır skor. */
        val ZERO = WordScore(base = 0)
    }
}
