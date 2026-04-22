package com.saffet.wordcrushmobile.domain.engine

import com.saffet.wordcrushmobile.domain.model.SpecialType
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [PowerUpRule] için PDF §6 tablosuna göre kapsama testi.
 */
class PowerUpRuleTest {

    @Test
    fun `3 harf ve altinda ozel guc bulunmaz`() {
        assertEquals(SpecialType.NONE, PowerUpRule.forWordLength(0))
        assertEquals(SpecialType.NONE, PowerUpRule.forWordLength(1))
        assertEquals(SpecialType.NONE, PowerUpRule.forWordLength(2))
        assertEquals(SpecialType.NONE, PowerUpRule.forWordLength(3))
    }

    @Test
    fun `4 harf - satir temizleme`() {
        assertEquals(SpecialType.ROW_CLEAR, PowerUpRule.forWordLength(4))
    }

    @Test
    fun `5 harf - alan patlatma`() {
        assertEquals(SpecialType.AREA_BLAST, PowerUpRule.forWordLength(5))
    }

    @Test
    fun `6 harf - sutun temizleme`() {
        assertEquals(SpecialType.COLUMN_CLEAR, PowerUpRule.forWordLength(6))
    }

    @Test
    fun `7 harf ve uzeri - mega patlatma`() {
        assertEquals(SpecialType.MEGA_BLAST, PowerUpRule.forWordLength(7))
        assertEquals(SpecialType.MEGA_BLAST, PowerUpRule.forWordLength(8))
        assertEquals(SpecialType.MEGA_BLAST, PowerUpRule.forWordLength(12))
    }

    @Test
    fun `negatif uzunluk guvenli sekilde NONE doner`() {
        assertEquals(SpecialType.NONE, PowerUpRule.forWordLength(-1))
        assertEquals(SpecialType.NONE, PowerUpRule.forWordLength(-100))
    }
}
