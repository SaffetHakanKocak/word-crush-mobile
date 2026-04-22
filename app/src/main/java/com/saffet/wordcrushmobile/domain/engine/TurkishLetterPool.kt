package com.saffet.wordcrushmobile.domain.engine

import kotlin.random.Random

/**
 * Türkçe harf üretim havuzu.
 *
 * Şartnameye göre harfler üç sıklık grubuna ayrılır:
 *  - Yüksek: A, E, İ, L, R, N
 *  - Orta:   K, M, T, S, Y, D
 *  - Düşük:  J, Ğ, F, V
 *
 * Listede belirtilmeyen Türkçe harflere (B, C, Ç, G, H, I, O, Ö, P, Ş, U, Ü, Z)
 * makul orta-düşük ağırlıklar verilmiştir. Böylece tahtada çeşitlilik korunur
 * ama kelime oluşturma şansı da yüksek kalır.
 *
 * Ağırlıklı rastgele seçim "roulette wheel" algoritmasıyla yapılır:
 * tüm ağırlıkların toplamı alınır, [0, totalWeight) aralığında bir sayı üretilir,
 * ağırlıklar tek tek çıkarılarak ilgili harf bulunur. O(n) ama n küçük olduğu
 * için pratikte maliyetsizdir.
 */
internal object TurkishLetterPool {

    private val weights: Map<Char, Int> = linkedMapOf(
        // --- Yüksek frekans ---
        'A' to 12,
        'E' to 10,
        'İ' to 8,
        'L' to 7,
        'R' to 7,
        'N' to 7,

        // --- Orta frekans ---
        'K' to 5,
        'T' to 5,
        'M' to 4,
        'S' to 4,
        'Y' to 4,
        'D' to 4,

        // --- Şartnamede geçmeyen, yaygın Türkçe harfler ---
        'I' to 3,
        'O' to 3,
        'U' to 3,
        'B' to 2,
        'C' to 2,
        'Ç' to 2,
        'G' to 2,
        'H' to 2,
        'P' to 2,
        'Ş' to 2,
        'Z' to 2,
        'Ö' to 1,
        'Ü' to 1,

        // --- Düşük frekans ---
        'F' to 1,
        'V' to 1,
        'Ğ' to 1,
        'J' to 1
    )

    private val totalWeight: Int = weights.values.sum()

    /** Tek bir ağırlıklı rastgele harf döndürür. */
    fun randomLetter(random: Random = Random.Default): Char {
        var pick = random.nextInt(totalWeight)
        for ((letter, weight) in weights) {
            pick -= weight
            if (pick < 0) return letter
        }
        error("Harf havuzu ağırlıkları tutarsız: pick=$pick, total=$totalWeight")
    }

    /** Verilen harfin havuzdaki ağırlığını döndürür; yoksa 0. */
    fun weightOf(letter: Char): Int = weights[letter] ?: 0
}
