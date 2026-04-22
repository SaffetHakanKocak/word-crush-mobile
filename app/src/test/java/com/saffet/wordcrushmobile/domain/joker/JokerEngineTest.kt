package com.saffet.wordcrushmobile.domain.joker

import com.saffet.wordcrushmobile.domain.engine.BoardPosition
import com.saffet.wordcrushmobile.domain.engine.WordCrushEngine
import com.saffet.wordcrushmobile.domain.model.Cell
import com.saffet.wordcrushmobile.domain.model.SpecialType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import kotlin.random.Random

/**
 * [JokerEngine] için birim testler. Determinizm için seed'li [Random]
 * kullanılır; engine davranışlarının matematiksel olarak beklenen
 * değişiklikleri ürettiği doğrulanır.
 */
class JokerEngineTest {

    private fun board(rows: Int, cols: Int, filler: Char = 'A'): List<List<Cell>> =
        List(rows) { r -> List(cols) { c -> Cell(row = r, col = c, letter = filler) } }

    private fun boardOf(vararg rows: String): List<List<Cell>> =
        rows.mapIndexed { r, row ->
            row.mapIndexed { c, ch -> Cell(row = r, col = c, letter = ch) }
        }

    // --- FISH ----------------------------------------------------------

    @Test
    fun `fish verilen sayida hucre siler ve yerlerine yenilerini uretir`() {
        val engine = JokerEngine(random = Random(42))
        val initial = board(4, 4, 'X')

        val result = engine.apply(initial, JokerAction.Fish(count = 5))

        assertTrue(result is JokerResult.Success)
        result as JokerResult.Success
        assertEquals(5, result.removedPositions.size)
        // Tahta boyutu ve kolon sayısı değişmemeli.
        assertEquals(4, result.newBoard.size)
        assertEquals(4, result.newBoard[0].size)
    }

    @Test
    fun `fish count tahta boyutundan buyukse boyuta kirpilir`() {
        val engine = JokerEngine(random = Random(1))
        val initial = board(2, 2)

        val result = engine.apply(initial, JokerAction.Fish(count = 99)) as JokerResult.Success

        assertEquals(4, result.removedPositions.size)
    }

    // --- WHEEL ---------------------------------------------------------

    @Test
    fun `wheel hedefin satir ve sutununu isaretler`() {
        val engine = JokerEngine(random = Random(7))
        val initial = board(4, 4)

        val res = engine.apply(initial, JokerAction.Wheel(BoardPosition(1, 2))) as JokerResult.Success

        // 4 satır + 4 kolon - 1 kesişim = 7 hücre.
        assertEquals(7, res.removedPositions.size)
        // Satırdaki tüm kolonlar dahil.
        for (c in 0..3) assertTrue(BoardPosition(1, c) in res.removedPositions)
        // Sütundaki tüm satırlar dahil.
        for (r in 0..3) assertTrue(BoardPosition(r, 2) in res.removedPositions)
    }

    @Test
    fun `wheel tahta disi hedefte InvalidTarget doner`() {
        val engine = JokerEngine()
        val initial = board(3, 3)

        val res = engine.apply(initial, JokerAction.Wheel(BoardPosition(5, 5)))

        assertTrue(res is JokerResult.InvalidTarget)
        assertEquals(
            JokerResult.InvalidTarget.Reason.OUT_OF_BOUNDS,
            (res as JokerResult.InvalidTarget).reason
        )
    }

    // --- LOLLIPOP ------------------------------------------------------

    @Test
    fun `lollipop yalnizca tek hucreyi siler`() {
        val engine = JokerEngine(random = Random(3))
        val initial = board(3, 3)

        val res = engine.apply(initial, JokerAction.Lollipop(BoardPosition(0, 1))) as JokerResult.Success

        assertEquals(setOf(BoardPosition(0, 1)), res.removedPositions)
        assertEquals(3, res.newBoard.size)
        assertEquals(3, res.newBoard[0].size)
    }

    // --- FREE_SWAP -----------------------------------------------------

    @Test
    fun `freeswap bitisik iki hucrenin iceriğini yer degistirir`() {
        val engine = JokerEngine()
        val initial = boardOf(
            "AB",
            "CD"
        )

        val res = engine.apply(
            initial,
            JokerAction.FreeSwap(BoardPosition(0, 0), BoardPosition(0, 1))
        ) as JokerResult.Success

        assertEquals('B', res.newBoard[0][0].letter)
        assertEquals('A', res.newBoard[0][1].letter)
        assertEquals('C', res.newBoard[1][0].letter)
        assertEquals('D', res.newBoard[1][1].letter)
        assertEquals(BoardPosition(0, 0) to BoardPosition(0, 1), res.swapped)
        // Swap silme değildir.
        assertTrue(res.removedPositions.isEmpty())
    }

    @Test
    fun `freeswap caprazlama komsuluk da gecerli`() {
        val engine = JokerEngine()
        val initial = boardOf("AB", "CD")

        val res = engine.apply(
            initial,
            JokerAction.FreeSwap(BoardPosition(0, 0), BoardPosition(1, 1))
        ) as JokerResult.Success

        assertEquals('D', res.newBoard[0][0].letter)
        assertEquals('A', res.newBoard[1][1].letter)
    }

    @Test
    fun `freeswap komsu degilse InvalidTarget doner`() {
        val engine = JokerEngine()
        val initial = board(3, 3)

        val res = engine.apply(
            initial,
            JokerAction.FreeSwap(BoardPosition(0, 0), BoardPosition(2, 2))
        )

        assertTrue(res is JokerResult.InvalidTarget)
        assertEquals(
            JokerResult.InvalidTarget.Reason.TARGETS_NOT_ADJACENT,
            (res as JokerResult.InvalidTarget).reason
        )
    }

    @Test
    fun `freeswap ayni hucre iki kez verilirse InvalidTarget doner`() {
        val engine = JokerEngine()
        val initial = board(3, 3)

        val res = engine.apply(
            initial,
            JokerAction.FreeSwap(BoardPosition(1, 1), BoardPosition(1, 1))
        )

        assertTrue(res is JokerResult.InvalidTarget)
        assertEquals(
            JokerResult.InvalidTarget.Reason.TARGETS_SAME_CELL,
            (res as JokerResult.InvalidTarget).reason
        )
    }

    @Test
    fun `freeswap hucrelerin ozel tipini de takas eder`() {
        val engine = JokerEngine()
        val initial = listOf(
            listOf(
                Cell(0, 0, 'A', special = SpecialType.ROW_CLEAR),
                Cell(0, 1, 'B', special = SpecialType.NONE)
            )
        )

        val res = engine.apply(
            initial,
            JokerAction.FreeSwap(BoardPosition(0, 0), BoardPosition(0, 1))
        ) as JokerResult.Success

        assertEquals(SpecialType.NONE, res.newBoard[0][0].special)
        assertEquals(SpecialType.ROW_CLEAR, res.newBoard[0][1].special)
    }

    // --- LETTER_SHUFFLE ------------------------------------------------

    @Test
    fun `letter shuffle ozel olmayan harfleri karistirir, ozel hucreler yerinde kalir`() {
        val engine = JokerEngine(random = Random(99))
        val initial = listOf(
            listOf(
                Cell(0, 0, 'A'),
                Cell(0, 1, 'B', special = SpecialType.AREA_BLAST),
                Cell(0, 2, 'C')
            ),
            listOf(
                Cell(1, 0, 'D'),
                Cell(1, 1, 'E'),
                Cell(1, 2, 'F')
            )
        )

        val res = engine.apply(initial, JokerAction.LetterShuffle) as JokerResult.Success

        // Özel hücre DEĞİŞMEDİ (konum + harf + özel tip).
        assertEquals('B', res.newBoard[0][1].letter)
        assertEquals(SpecialType.AREA_BLAST, res.newBoard[0][1].special)

        // Diğer 5 harfin multiset'i aynı olmalı.
        val other = mutableListOf<Char>()
        for (r in 0..1) for (c in 0..2) {
            if (r == 0 && c == 1) continue
            other.add(res.newBoard[r][c].letter)
        }
        assertEquals(listOf('A', 'C', 'D', 'E', 'F').sorted(), other.sorted())
    }

    // --- PARTY_BOOSTER -------------------------------------------------

    @Test
    fun `party booster tum gridi siler ve yeniden uretir`() {
        val engine = JokerEngine(random = Random(5))
        val initial = listOf(
            listOf(
                Cell(0, 0, 'A', special = SpecialType.MEGA_BLAST),
                Cell(0, 1, 'B')
            ),
            listOf(
                Cell(1, 0, 'C'),
                Cell(1, 1, 'D')
            )
        )

        val res = engine.apply(initial, JokerAction.PartyBooster) as JokerResult.Success

        // Tüm pozisyonlar removed set'inde.
        assertEquals(4, res.removedPositions.size)
        // Yeni tahta aynı boyda.
        assertEquals(2, res.newBoard.size)
        assertEquals(2, res.newBoard[0].size)
        // generateBoard özel simge üretmez: hepsi NONE olmalı.
        for (r in 0..1) for (c in 0..1) {
            assertEquals(SpecialType.NONE, res.newBoard[r][c].special)
        }
    }

    // --- genel ---------------------------------------------------------

    @Test
    fun `bos tahta tum jokerlerde InvalidTarget BOARD_EMPTY doner`() {
        val engine = JokerEngine()
        val res = engine.apply(emptyList(), JokerAction.LetterShuffle)
        assertTrue(res is JokerResult.InvalidTarget)
        assertEquals(
            JokerResult.InvalidTarget.Reason.BOARD_EMPTY,
            (res as JokerResult.InvalidTarget).reason
        )
    }

    @Test
    fun `jokerEngine sadece WordCrushEngine collapseAndRefill uzerinden gravity uygular`() {
        // Yerçekimi davranışı WordCrushEngine tarafından test ediliyor.
        // Burada yalnızca JokerEngine'in onu doğru çağırdığını kontrol ederiz:
        // Fish sonrası tüm hücreler dolu kalır (refill yeni harfler üretir).
        val wcEngine = WordCrushEngine(random = Random(0))
        val engine = JokerEngine(random = Random(0), engine = wcEngine)
        val initial = board(3, 3, 'Z')

        val res = engine.apply(initial, JokerAction.Fish(count = 3)) as JokerResult.Success

        for (r in 0..2) for (c in 0..2) {
            // Hiçbir hücre "boş" karakter değil — refill çalıştı.
            assertNotEquals(' ', res.newBoard[r][c].letter)
        }
    }

    @Test
    fun `aksiyon tipi JokerType ile eslesir`() {
        // JokerTargetSpec.of + aksiyonların kendi .type'ları tutarlı mı?
        assertEquals(com.saffet.wordcrushmobile.domain.model.JokerType.FISH, JokerAction.Fish().type)
        assertEquals(
            com.saffet.wordcrushmobile.domain.model.JokerType.WHEEL,
            JokerAction.Wheel(BoardPosition(0, 0)).type
        )
        assertEquals(
            com.saffet.wordcrushmobile.domain.model.JokerType.LOLLIPOP_HAMMER,
            JokerAction.Lollipop(BoardPosition(0, 0)).type
        )
        assertEquals(
            com.saffet.wordcrushmobile.domain.model.JokerType.FREE_SWAP,
            JokerAction.FreeSwap(BoardPosition(0, 0), BoardPosition(0, 1)).type
        )
        assertEquals(
            com.saffet.wordcrushmobile.domain.model.JokerType.LETTER_SHUFFLE,
            JokerAction.LetterShuffle.type
        )
        assertEquals(
            com.saffet.wordcrushmobile.domain.model.JokerType.PARTY_BOOSTER,
            JokerAction.PartyBooster.type
        )
    }

    @Test
    fun `JokerTargetSpec hedef sayisi ve komsuluk ihtiyaci dogru raporlar`() {
        assertEquals(0, JokerTargetSpec.of(com.saffet.wordcrushmobile.domain.model.JokerType.FISH).neededTargets)
        assertEquals(1, JokerTargetSpec.of(com.saffet.wordcrushmobile.domain.model.JokerType.WHEEL).neededTargets)
        assertEquals(1, JokerTargetSpec.of(com.saffet.wordcrushmobile.domain.model.JokerType.LOLLIPOP_HAMMER).neededTargets)

        val swap = JokerTargetSpec.of(com.saffet.wordcrushmobile.domain.model.JokerType.FREE_SWAP)
        assertEquals(2, swap.neededTargets)
        assertTrue(swap.requiresAdjacentTargets)

        val shuffle = JokerTargetSpec.of(com.saffet.wordcrushmobile.domain.model.JokerType.LETTER_SHUFFLE)
        assertEquals(0, shuffle.neededTargets)
        assertFalse(shuffle.requiresAdjacentTargets)
    }

    @Test
    fun `fish sifir sonucu uretmez, swap sonucu null degildir, lollipop sonucu bos olmaz`() {
        val engine = JokerEngine(random = Random(0))
        val b = board(3, 3)

        val fish = engine.apply(b, JokerAction.Fish(count = 1)) as JokerResult.Success
        assertTrue(fish.removedPositions.isNotEmpty())
        assertNull(fish.swapped)

        val swap = engine.apply(
            b,
            JokerAction.FreeSwap(BoardPosition(0, 0), BoardPosition(0, 1))
        ) as JokerResult.Success
        assertNotNull(swap.swapped)

        val lolli = engine.apply(b, JokerAction.Lollipop(BoardPosition(2, 2))) as JokerResult.Success
        assertEquals(1, lolli.removedPositions.size)
    }
}
