package com.saffet.wordcrushmobile.domain.combo

/**
 * Bir kelime submit edildiğinde üretilen combo analiz sonucu.
 *
 * PDF §Combo Mekaniği:
 *  - Ana kelime daima combo'ya dahildir.
 *  - Ana kelimenin içindeki 3+ harfli, sıraya uygun ve anlamlı alt kelimeler
 *    de combo'ya dahil edilir.
 *  - Aynı alt kelime birden fazla sayılmaz (dedupe).
 *  - Alt kelimelerin puanları ana skora ayrıca eklenir.
 *
 * @property mainWord Combo'yu başlatan ana kelime (büyük harf, normalize).
 * @property subWords Sözlükte bulunmuş, benzersiz, 3+ harfli alt kelimeler.
 *                    Ana kelime bu listede BULUNMAZ — [allWords] ile birlikte
 *                    gelir. Listeleme sırası deterministik: üretim sırasına
 *                    (kelime başından ve kısa→uzun) uygun.
 */
data class ComboResult(
    val mainWord: String,
    val subWords: List<String>
) {
    /** Ana kelime + alt kelimeler. UI "combo listesi" göstermek için kullanır. */
    val allWords: List<String> get() = listOf(mainWord) + subWords

    /** PDF'deki "N× combo" ifadesindeki N değeri (ana kelime + alt kelimeler). */
    val comboCount: Int get() = 1 + subWords.size

    /** Birden fazla kelime çözülmüşse combo aktif sayılır. */
    val isCombo: Boolean get() = subWords.isNotEmpty()

    companion object {
        /** Yalnızca ana kelimeyi içeren (alt kelime yok) sonucu üretir. */
        fun single(mainWord: String): ComboResult =
            ComboResult(mainWord = mainWord, subWords = emptyList())
    }
}
