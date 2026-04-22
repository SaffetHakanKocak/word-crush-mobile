package com.saffet.wordcrushmobile.domain.engine

import com.saffet.wordcrushmobile.domain.model.Cell
import com.saffet.wordcrushmobile.domain.model.SpecialType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * [WordCrushEngine.applyWord] için PDF §6 "Harf Patlatma Mekaniği"
 * davranış testleri.
 *
 * Önemli odaklar:
 *  - Kelime uzunluğuna göre doğru [SpecialType] bırakılıyor mu?
 *  - Son harfin konumu "yerinde kaldığını" koruyor mu? (bu konum cleared
 *    olmuyor, özel tipiyle orada duruyor)
 *  - Seçimde mevcut bir özel hücre varsa etkisi tetikleniyor mu?
 *  - Collapse sonrası diğer hücreler yerçekimiyle aşağı düşüyor mu?
 */
class WordCrushEngineApplyWordTest {

    // Deterministik testler için sabit seed. collapseAndRefill yeni harf
    // üretirken bu kaynağı kullanır; assert'lar yeni harflerin değerine
    // BAĞLI DEĞİLDİR — yalnızca pozisyon ve özel tipe bakılır.
    private val engine = WordCrushEngine(random = Random(seed = 42))

    /**
     * Satır metinlerinden Cell matrisi üretir. `specials` map'i belirli
     * konumların özel tiplerini ayarlar.
     */
    private fun board(
        vararg rows: String,
        specials: Map<Pair<Int, Int>, SpecialType> = emptyMap()
    ): List<List<Cell>> =
        rows.mapIndexed { r, row ->
            row.mapIndexed { c, ch ->
                Cell(
                    row = r,
                    col = c,
                    letter = ch,
                    special = specials[r to c] ?: SpecialType.NONE
                )
            }
        }

    private fun selection(board: List<List<Cell>>, vararg rc: Pair<Int, Int>): List<Cell> =
        rc.map { (r, c) -> board[r][c] }

    // --- Özel güç bırakma (planted) ----------------------------------

    @Test
    fun `3 harfli kelime ozel guc birakmaz`() {
        val b = board("ali", "xxx", "xxx")
        val sel = selection(b, 0 to 0, 0 to 1, 0 to 2)

        val res = engine.applyWord(b, sel)

        assertNull("3 harf için planted olmamalı", res.plantedSpecial)
        // Seçimin tamamı silinmiş olmalı → 3 pozisyon.
        assertEquals(3, res.removedPositions.size)
        assertTrue(BoardPosition(0, 2) in res.removedPositions)
    }

    @Test
    fun `4 harfli kelime ROW_CLEAR birakir son harfe`() {
        val b = board("abcd", "xxxx", "xxxx")
        val sel = selection(b, 0 to 0, 0 to 1, 0 to 2, 0 to 3)

        val res = engine.applyWord(b, sel)

        assertNotNull(res.plantedSpecial)
        assertEquals(SpecialType.ROW_CLEAR, res.plantedSpecial!!.type)
        // Son hücre: (0, 3)
        assertEquals(0, res.plantedSpecial!!.row)
        assertEquals(3, res.plantedSpecial!!.col)
        // Yeni board'da (0, 3) konumunda ROW_CLEAR bulunan hücre olmalı.
        assertEquals(SpecialType.ROW_CLEAR, res.newBoard[0][3].special)
    }

    @Test
    fun `5 harfli kelime AREA_BLAST birakir`() {
        val b = board("abcde", "xxxxx", "xxxxx")
        val sel = selection(b, 0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4)
        val res = engine.applyWord(b, sel)
        assertEquals(SpecialType.AREA_BLAST, res.plantedSpecial?.type)
        assertEquals(SpecialType.AREA_BLAST, res.newBoard[0][4].special)
    }

    @Test
    fun `6 harfli kelime COLUMN_CLEAR birakir`() {
        val b = board("abcdef", "xxxxxx", "xxxxxx")
        val sel = selection(b, 0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4, 0 to 5)
        val res = engine.applyWord(b, sel)
        assertEquals(SpecialType.COLUMN_CLEAR, res.plantedSpecial?.type)
    }

    @Test
    fun `7 harfli kelime MEGA_BLAST birakir`() {
        val b = board("abcdefg", "xxxxxxx", "xxxxxxx")
        val sel = selection(
            b, 0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4, 0 to 5, 0 to 6
        )
        val res = engine.applyWord(b, sel)
        assertEquals(SpecialType.MEGA_BLAST, res.plantedSpecial?.type)
    }

    // --- Son harfin "yerinde kalması" --------------------------------

    @Test
    fun `ozel birakilan son hucre removedPositions icinde olmaz`() {
        // 4 harf → ROW_CLEAR bırakılır, son hücre (0,3) korunmalı.
        val b = board("abcd", "xxxx", "xxxx")
        val sel = selection(b, 0 to 0, 0 to 1, 0 to 2, 0 to 3)

        val res = engine.applyWord(b, sel)

        assertFalse(
            "PDF: son harf yerinde kalır; cleared'a girmemeli",
            BoardPosition(0, 3) in res.removedPositions
        )
        // Diğer 3 hücre silinmiş olmalı.
        assertTrue(BoardPosition(0, 0) in res.removedPositions)
        assertTrue(BoardPosition(0, 1) in res.removedPositions)
        assertTrue(BoardPosition(0, 2) in res.removedPositions)
    }

    @Test
    fun `ozel birakilan son hucre orijinal harfini korur`() {
        val b = board("abcd", "xxxx", "xxxx")
        val sel = selection(b, 0 to 0, 0 to 1, 0 to 2, 0 to 3)

        val res = engine.applyWord(b, sel)

        // (0,3) konumunda 'd' harfi + ROW_CLEAR özeli olmalı.
        val at = res.newBoard[0][3]
        assertEquals('d', at.letter)
        assertEquals(SpecialType.ROW_CLEAR, at.special)
        // Bu bir "zincirleme collapse sınırı" olduğu için üstündeki
        // harflerden ETKİLENMEZ, konumu (row=0, col=3) olarak kalır.
        assertEquals(0, at.row)
        assertEquals(3, at.col)
    }

    // --- Tetikleme: seçimdeki özel hücre aktive olur ------------------

    @Test
    fun `secimdeki ROW_CLEAR satiri tamamen patlatir`() {
        // 3 harfli dikey seçim satır 1'de biter; ancak seçimin SON hücresi
        // (satır 1) ROW_CLEAR özelini taşır. Satır 1'in SEÇIM DIŞI kolonları
        // da patlamalı — asıl testin istediği budur.
        val b = board(
            "xxxxx",
            "yaxxx",  // a (1,1) ROW_CLEAR
            "xbxxx",
            "xcxxx",
            specials = mapOf((1 to 1) to SpecialType.ROW_CLEAR)
        )
        // Seçim: c(3,1) → b(2,1) → a(1,1). Son hücre = a, ROW_CLEAR.
        val sel = selection(b, 3 to 1, 2 to 1, 1 to 1)

        val res = engine.applyWord(b, sel)

        assertTrue(SpecialType.ROW_CLEAR in res.triggeredSpecials)
        // Satır 1'in tamamı temizlenmeli — seçim yalnızca (1,1)'i içeriyor,
        // ama ROW_CLEAR sayesinde (1,0), (1,2), (1,3), (1,4) de cleared olur.
        for (c in 0..4) {
            assertTrue(
                "Satır 1 kolonu $c silinmedi",
                BoardPosition(1, c) in res.removedPositions
            )
        }
        // 3 harf → yeni özel bırakılmaz.
        assertNull(res.plantedSpecial)
    }

    @Test
    fun `secimdeki AREA_BLAST merkez etrafini patlatir`() {
        val b = board(
            "xxxxx",
            "xaxxx",
            "xbxxx",
            "xcxxx",
            "xxxxx",
            specials = mapOf((3 to 1) to SpecialType.AREA_BLAST)
        )
        // Dikey seçim: a(1,1) → b(2,1) → c(3,1). 3 harf → planted yok.
        // Son hücre c AREA_BLAST: (3,1) merkezli 3x3 → (2..4, 0..2) da patlar.
        val sel = selection(b, 1 to 1, 2 to 1, 3 to 1)
        val res = engine.applyWord(b, sel)

        assertTrue(SpecialType.AREA_BLAST in res.triggeredSpecials)
        // 3x3 alan patlamalı:
        for (r in 2..4) for (c in 0..2) {
            assertTrue(
                "($r,$c) patlamaliydi",
                BoardPosition(r, c) in res.removedPositions
            )
        }
        // Seçim harfleri de patlamalı.
        assertTrue(BoardPosition(1, 1) in res.removedPositions)
    }

    @Test
    fun `4 harfli kelime AYNI anda hem triggered hem planted verebilir`() {
        // Seçimin ilk hücresi ROW_CLEAR özeli taşısın, 4 harfli olsun.
        // Beklenti: ROW_CLEAR tetiklenir (triggered) + son hücreye YENİ
        // ROW_CLEAR bırakılır (planted).
        val b = board(
            "abcd",
            "xxxx",
            "xxxx",
            specials = mapOf((0 to 0) to SpecialType.ROW_CLEAR)
        )
        val sel = selection(b, 0 to 0, 0 to 1, 0 to 2, 0 to 3)

        val res = engine.applyWord(b, sel)

        assertTrue(SpecialType.ROW_CLEAR in res.triggeredSpecials)
        assertNotNull(res.plantedSpecial)
        assertEquals(SpecialType.ROW_CLEAR, res.plantedSpecial!!.type)
        assertEquals(0 to 3, res.plantedSpecial!!.row to res.plantedSpecial!!.col)

        // Planted konumu cleared olmamalı.
        assertFalse(BoardPosition(0, 3) in res.removedPositions)
    }

    // --- Yerçekimi / collapse --------------------------------------------

    @Test
    fun `ozelsiz kelime sonrasinda dokunulmamis sutunlar aynen kalir`() {
        // col 1: dikey 3 harfli kelime seçilir, tüm sütun silinir. PDF:
        // diğer sütunlar dokunulmamalı; test buna odaklanır (yeni harflerin
        // değerini sınamak TurkishLetterPool'a bağlı olduğu için kırılgan
        // olur — bu yüzden sadece üç tarafı kontrol ediyoruz).
        val b = board(
            "apx",
            "bqy",
            "crz"
        )
        val sel = selection(b, 0 to 1, 1 to 1, 2 to 1)

        val res = engine.applyWord(b, sel)

        assertNull(res.plantedSpecial)
        // removedPositions tam olarak 3 hücre içermeli: col 1'in tamamı.
        assertEquals(setOf(
            BoardPosition(0, 1),
            BoardPosition(1, 1),
            BoardPosition(2, 1)
        ), res.removedPositions)

        // 0. ve 2. sütunlar dokunulmamış.
        assertEquals('a', res.newBoard[0][0].letter)
        assertEquals('b', res.newBoard[1][0].letter)
        assertEquals('c', res.newBoard[2][0].letter)
        assertEquals('x', res.newBoard[0][2].letter)
        assertEquals('y', res.newBoard[1][2].letter)
        assertEquals('z', res.newBoard[2][2].letter)
    }

    @Test
    fun `preserved hucre zincirli collapse icin sinir gorevi gorur`() {
        // 4 harfli yatay kelime: (2, 0..3). ROW_CLEAR bırakılır → (2,3)
        // yerinde kalır. Üstündeki (0,3) ve (1,3) ne olur?
        // Algoritma her sütunu preserve sınırıyla segmentlere böler:
        //  - col 3 için segment [0..2) cleared'a uğramaz, yaşayanlar:
        //    board[0][3], board[1][3] → alta kayar (2 yaşayan, 2 slot).
        //  - col 3 satır 2 → FIXED (preserve).
        //  - (Geride segment yok.)
        // Sonuçta yeni board[0][3] ve board[1][3] = eski board[0][3], board[1][3]
        // (düşme olmaz çünkü tam 2 yaşayan 2 slota sığıyor).
        val b = board(
            "...p",
            "...q",
            "abcd"
        )
        val sel = selection(b, 2 to 0, 2 to 1, 2 to 2, 2 to 3)
        val res = engine.applyWord(b, sel)

        // (2,3) FIXED → 'd' + ROW_CLEAR yerinde.
        assertEquals('d', res.newBoard[2][3].letter)
        assertEquals(SpecialType.ROW_CLEAR, res.newBoard[2][3].special)

        // (0,3), (1,3) dokunulmadı.
        assertEquals('p', res.newBoard[0][3].letter)
        assertEquals('q', res.newBoard[1][3].letter)

        // (2,0), (2,1), (2,2) cleared → üstündeki harfler aşağı düştü.
        // col 0: board[0][0]='.', board[1][0]='.', board[2][0]='a' cleared.
        //   Yaşayanlar (üstten alta): '.', '.' → 2 yaşayan, 3 slot, 1 yeni.
        //   Yeni board col 0: [yeni, '.', '.'].
        assertEquals('.', res.newBoard[1][0].letter)
        assertEquals('.', res.newBoard[2][0].letter)
    }
}
