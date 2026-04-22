package com.saffet.wordcrushmobile.domain.score

import java.util.Locale

/**
 * Türkçe harflerin puan tablosu — şartnameye birebir uygun.
 *
 * Kaynak: yazlab_mobil.pdf (Bölüm 5, Harf Puan Tablosu).
 *
 * | A | B | C | Ç | D | E | F | G | Ğ | H | I | İ | J  | K | L |
 * |:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:--:|:-:|:-:|
 * | 1 | 3 | 4 | 4 | 3 | 1 | 7 | 5 | 8 | 5 | 2 | 1 | 10 | 1 | 1 |
 *
 * | M | N | O | Ö | P | R | S | Ş | T | U | Ü | V | Y | Z |
 * |:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
 * | 2 | 1 | 2 | 7 | 5 | 1 | 2 | 4 | 1 | 2 | 3 | 7 | 3 | 4 |
 *
 * Örnek: "soru" → s(2) + o(2) + r(1) + u(2) = 7 puan.
 *
 * Saf Kotlin'dir; doğrudan JVM testlerinde kullanılabilir.
 */
object TurkishLetterScores : LetterScores {

    private val TURKISH: Locale = Locale.forLanguageTag("tr-TR")

    private val points: Map<Char, Int> = mapOf(
        'a' to 1, 'b' to 3, 'c' to 4, 'ç' to 4, 'd' to 3,
        'e' to 1, 'f' to 7, 'g' to 5, 'ğ' to 8, 'h' to 5,
        'ı' to 2, 'i' to 1, 'j' to 10, 'k' to 1, 'l' to 1,
        'm' to 2, 'n' to 1, 'o' to 2, 'ö' to 7, 'p' to 5,
        'r' to 1, 's' to 2, 'ş' to 4, 't' to 1, 'u' to 2,
        'ü' to 3, 'v' to 7, 'y' to 3, 'z' to 4
    )

    /**
     * Harfin puanını döndürür. Büyük/küçük harf farkı önemsenmez;
     * Türkçe locale ile lowercase edilir.
     *
     * Tabloda olmayan bir karakter için 0 döner (ör. rakam, boşluk).
     */
    override fun scoreOf(letter: Char): Int {
        val normalized = letter.toString().lowercase(TURKISH).firstOrNull() ?: return 0
        return points[normalized] ?: 0
    }
}

/**
 * Harflerin puan kaynağı. Oyunun geri kalanı yalnızca bu arayüzü
 * kullanır — böylece testlerde sahte bir puan tablosuyla (ör. tüm harfler 1)
 * koşturmak ya da ileride farklı bir dil için alternatif tablo eklemek
 * kolaylaşır.
 */
interface LetterScores {
    fun scoreOf(letter: Char): Int
}
