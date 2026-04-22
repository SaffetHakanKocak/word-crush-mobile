package com.saffet.wordcrushmobile.domain.engine

import com.saffet.wordcrushmobile.domain.model.SpecialType

/**
 * PDF §6 "Harf Patlatma Mekaniği" tablosunu Kotlin'e taşıyan saf kural objesi.
 *
 * | Kelime Uzunluğu | Özel Güç          | [SpecialType]             |
 * |----------------:|-------------------|---------------------------|
 * | < 4             | (yok)             | [SpecialType.NONE]        |
 * | 4               | Satır Temizleme   | [SpecialType.ROW_CLEAR]   |
 * | 5               | Alan Patlatma     | [SpecialType.AREA_BLAST]  |
 * | 6               | Sütun Temizleme   | [SpecialType.COLUMN_CLEAR]|
 * | ≥ 7             | Mega Patlatma     | [SpecialType.MEGA_BLAST]  |
 *
 * Stateless/singleton — test edilebilirliği azami seviyede.
 */
object PowerUpRule {

    /**
     * Verilen [wordLength] (kelime harf sayısı) için bırakılacak özel
     * hücre tipini döner. 4 harften kısa kelimeler özel güç bırakmaz.
     */
    fun forWordLength(wordLength: Int): SpecialType = when {
        wordLength <= 0 -> SpecialType.NONE
        wordLength >= 7 -> SpecialType.MEGA_BLAST
        wordLength == 6 -> SpecialType.COLUMN_CLEAR
        wordLength == 5 -> SpecialType.AREA_BLAST
        wordLength == 4 -> SpecialType.ROW_CLEAR
        else -> SpecialType.NONE
    }
}
