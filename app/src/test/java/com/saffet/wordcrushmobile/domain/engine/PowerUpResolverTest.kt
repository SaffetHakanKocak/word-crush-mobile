package com.saffet.wordcrushmobile.domain.engine

import com.saffet.wordcrushmobile.domain.model.Cell
import com.saffet.wordcrushmobile.domain.model.SpecialType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [PowerUpResolver] için testler: her özel gücün etki kümesinin
 * PDF §6 açıklamasıyla uyumlu olduğunu doğrular.
 */
class PowerUpResolverTest {

    private val resolver = PowerUpResolver()

    /**
     * Verilen boyutlarda, tüm hücreleri `x` olan test tahtası; merkez
     * hücrenin özel tipi parametre ile belirlenir.
     */
    private fun board(
        rows: Int,
        cols: Int,
        originRow: Int,
        originCol: Int,
        type: SpecialType
    ): Pair<List<List<Cell>>, Cell> {
        val grid = List(rows) { r ->
            List(cols) { c ->
                val special = if (r == originRow && c == originCol) type else SpecialType.NONE
                Cell(row = r, col = c, letter = 'x', special = special)
            }
        }
        return grid to grid[originRow][originCol]
    }

    // --- NONE -----------------------------------------------------------

    @Test
    fun `NONE - bos set doner`() {
        val (b, origin) = board(5, 5, 2, 2, SpecialType.NONE)
        assertTrue(resolver.affected(b, origin).isEmpty())
    }

    // --- ROW_CLEAR ------------------------------------------------------

    @Test
    fun `ROW_CLEAR - tum satir`() {
        val (b, origin) = board(6, 8, 3, 4, SpecialType.ROW_CLEAR)
        val affected = resolver.affected(b, origin)
        assertEquals(8, affected.size)
        for (c in 0 until 8) {
            assertTrue(BoardPosition(3, c) in affected)
        }
        // Başka satırlar etkilenmesin.
        assertFalse(BoardPosition(2, 4) in affected)
        assertFalse(BoardPosition(4, 4) in affected)
    }

    // --- COLUMN_CLEAR ---------------------------------------------------

    @Test
    fun `COLUMN_CLEAR - tum sutun`() {
        val (b, origin) = board(6, 8, 3, 4, SpecialType.COLUMN_CLEAR)
        val affected = resolver.affected(b, origin)
        assertEquals(6, affected.size)
        for (r in 0 until 6) {
            assertTrue(BoardPosition(r, 4) in affected)
        }
        assertFalse(BoardPosition(3, 3) in affected)
        assertFalse(BoardPosition(3, 5) in affected)
    }

    // --- AREA_BLAST -----------------------------------------------------

    @Test
    fun `AREA_BLAST - 3x3 alan merkez dahil`() {
        val (b, origin) = board(6, 6, 3, 3, SpecialType.AREA_BLAST)
        val affected = resolver.affected(b, origin)
        assertEquals(9, affected.size)
        for (dr in -1..1) for (dc in -1..1) {
            assertTrue(BoardPosition(3 + dr, 3 + dc) in affected)
        }
    }

    @Test
    fun `AREA_BLAST - kose hucre tahta disina tasmaz`() {
        // Köşede: (0,0) için etki (0,0), (0,1), (1,0), (1,1) → 4 hücre.
        val (b, origin) = board(6, 6, 0, 0, SpecialType.AREA_BLAST)
        val affected = resolver.affected(b, origin)
        assertEquals(4, affected.size)
        assertTrue(BoardPosition(0, 0) in affected)
        assertTrue(BoardPosition(0, 1) in affected)
        assertTrue(BoardPosition(1, 0) in affected)
        assertTrue(BoardPosition(1, 1) in affected)
    }

    // --- MEGA_BLAST -----------------------------------------------------

    @Test
    fun `MEGA_BLAST - 5x5 alan`() {
        val (b, origin) = board(8, 8, 4, 4, SpecialType.MEGA_BLAST)
        val affected = resolver.affected(b, origin)
        assertEquals(25, affected.size)
        for (dr in -2..2) for (dc in -2..2) {
            assertTrue(BoardPosition(4 + dr, 4 + dc) in affected)
        }
    }

    @Test
    fun `MEGA_BLAST - tahta kenarinda taslar kirpilir`() {
        val (b, origin) = board(6, 6, 1, 1, SpecialType.MEGA_BLAST)
        val affected = resolver.affected(b, origin)
        // (1,1) merkezli 5x5 alanın tahta içindeki kısmı:
        // satırlar 0..3, sütunlar 0..3 → 4x4 = 16 hücre.
        assertEquals(16, affected.size)
    }
}
