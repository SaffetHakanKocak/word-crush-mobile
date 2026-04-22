package com.saffet.wordcrushmobile.domain.engine

import com.saffet.wordcrushmobile.domain.dictionary.TrieWordDictionary
import com.saffet.wordcrushmobile.domain.dictionary.WordDictionary
import com.saffet.wordcrushmobile.domain.model.Cell
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [AvailableWordCounter] için birim testler.
 *
 * Amaç:
 *  - 8 yönlü DFS doğru hücreleri geziyor.
 *  - Aynı hücre iki kere kullanılmıyor (path-level unique).
 *  - Farklı yollardan aynı kelimeye varılsa bile sayaç tek sayıyor.
 *  - Prefix pruning "kelime bulma" sonucunu değiştirmiyor (aynı
 *    girdilerde Trie ile HashSet-benzeri non-prefix dict aynı sonucu
 *    vermeli — sadece hız farkı olmalı).
 */
class AvailableWordCounterTest {

    private val counter = AvailableWordCounter()

    // --- Yardımcılar --------------------------------------------------

    /**
     * Satır listesinden Cell matrisi üretir. Her satır aynı sütun sayısına
     * sahip olmalı; elemanlar tek karakterli [String] olarak verilir.
     */
    private fun board(vararg rows: String): List<List<Cell>> =
        rows.mapIndexed { r, row ->
            row.mapIndexed { c, ch ->
                Cell(row = r, col = c, letter = ch)
            }
        }

    /**
     * Prefix desteği olmayan naif bir sözlük; pruning yapamaz (hasPrefix
     * her zaman true döner). Sayımın doğruluğu prefix pruning olmadan da
     * garanti olmalı; bu yardımcı onu sınamak için kullanılır.
     */
    private class NoPrefixDictionary(words: Iterable<String>) : WordDictionary {
        private val set: Set<String> = words.toHashSet()
        override val size: Int = set.size
        override fun contains(word: String): Boolean = set.contains(word)
        // hasPrefix varsayılanı (true) kullanılır — pruning yok.
    }

    // --- Temel davranış ----------------------------------------------

    @Test
    fun `bos grid 0 doner`() {
        val dict = TrieWordDictionary(listOf("ali"))
        assertEquals(0, counter.count(emptyList(), dict))
    }

    @Test
    fun `tek satirda 3 harfli kelime bulunur`() {
        // A L İ yan yana → "ali" bulunur (tek kelime).
        val b = board("ali")
        val dict = TrieWordDictionary(listOf("ali"))
        assertEquals(1, counter.count(b, dict))
    }

    @Test
    fun `3 harften kisa kelimeler sayilmaz`() {
        val b = board("al")
        val dict = TrieWordDictionary(listOf("al"))
        assertEquals("2 harfli kelime sayıma dahil edilmemeli", 0, counter.count(b, dict))
    }

    @Test
    fun `sozlukte olmayan kelime sayilmaz`() {
        val b = board("xyz")
        val dict = TrieWordDictionary(listOf("ali"))
        assertEquals(0, counter.count(b, dict))
    }

    // --- Komşuluk / yol kuralları ------------------------------------

    @Test
    fun `ayni hucre bir yolda iki kez kullanilamaz`() {
        // Grid:
        // A L
        // L A   --> "ala" ancak aynı A iki kere kullanılmadan oluşamaz:
        //           (0,0)=A, (0,1)=L → buradan A'ya komşu yalnızca (1,0)=L
        //           dolayısıyla "ala" ÜSTTEKİ A'dan başlayıp ALTTAKİ A'ya
        //           gidilerek oluşur — iki farklı A hücresi vardır.
        val b = board(
            "al",
            "la"
        )
        val dict = TrieWordDictionary(listOf("ala"))
        assertEquals(1, counter.count(b, dict))
    }

    @Test
    fun `tek harfli izole hucre ile kelime olusmaz`() {
        // A X
        // X X   --> sözlükte "axx" yok, kimse bulunamaz.
        val b = board(
            "ax",
            "xx"
        )
        val dict = TrieWordDictionary(listOf("ali"))
        assertEquals(0, counter.count(b, dict))
    }

    @Test
    fun `8 yonlu komsuluk - capraz calisir`() {
        // A X
        // X L
        // ↳ (0,0)=A, (1,1)=L çapraz komşu; (0,1)=X üzerinden de gidilebilir.
        //   Sözlükte "al" yok (3 harften kısa zaten), "axl" yok.
        // Şimdi 3x3 ile:
        // k a r
        // x l x
        // x e m  → "kalem" çapraz komşuluklarla bulunur:
        //   k(0,0) → a(0,1) → l(1,1) → e(2,1) → m(2,2). Her adım 8-komşu.
        val b = board(
            "kar",
            "xlx",
            "xem"
        )
        val dict = TrieWordDictionary(listOf("kalem"))
        assertEquals(1, counter.count(b, dict))
    }

    // --- Benzersizlik -------------------------------------------------

    @Test
    fun `ayni kelimeye iki farkli yoldan varilsa bile tek sayilir`() {
        // a l i
        // l i a
        // i a l
        //
        // "ali", "ila", "aliya" vs. Tekrarlı harflerden ötürü aynı
        // kelimeye birden çok hücre dizilimiyle ulaşılır. Sayım
        // benzersiz kelimeyi (HashSet) kullanır → her biri bir kere sayılır.
        val b = board(
            "ali",
            "lia",
            "ial"
        )
        val dict = TrieWordDictionary(listOf("ali", "ila"))

        val result = counter.analyze(b, dict)
        assertTrue("ali" in result.words)
        assertTrue("ila" in result.words)
        assertEquals(2, result.count)
    }

    // --- Pruning tutarlılığı ------------------------------------------

    @Test
    fun `prefix pruning sonucun sayisini degistirmez`() {
        val words = listOf("ali", "ala", "aral", "alarm")
        val trie = TrieWordDictionary(words)
        val noPrefix = NoPrefixDictionary(words)

        val b = board(
            "aral",
            "lamx",
            "imxr"
        )

        val resTrie = counter.count(b, trie)
        val resNo = counter.count(b, noPrefix)
        assertEquals("Pruning'li ve pruningsiz hesap aynı sayıyı vermeli", resNo, resTrie)
    }

    // --- Sınırlar -----------------------------------------------------

    @Test
    fun `minLen parametresi dikkate alinir`() {
        val b = board(
            "ali",
            "lii"
        )
        val dict = TrieWordDictionary(listOf("ali", "ili"))

        val defaultRes = counter.count(b, dict) // min 3
        assertTrue(defaultRes >= 2)

        // minLen=4 yaparsak 3 harfli hiçbir kelime sayılmaz.
        val tighter = counter.count(
            b, dict,
            AvailableWordCounter.ComputeOptions(minLen = 4, maxLen = 10)
        )
        assertEquals(0, tighter)
    }

    @Test
    fun `maxLen path'i kisaltir ama kisa kelimeleri etkilemez`() {
        val b = board(
            "kar",
            "xlx",
            "xem"
        )
        val dict = TrieWordDictionary(listOf("kalem"))

        // maxLen < "kalem".length (5) → hiç bulunamaz.
        val cappedShort = counter.count(
            b, dict,
            AvailableWordCounter.ComputeOptions(minLen = 3, maxLen = 4)
        )
        assertEquals(0, cappedShort)

        // maxLen = 5 → tam sığar.
        val cappedOk = counter.count(
            b, dict,
            AvailableWordCounter.ComputeOptions(minLen = 3, maxLen = 5)
        )
        assertEquals(1, cappedOk)
    }

    @Test
    fun `dikdortgen olmayan board hata firlatir`() {
        val ragged = listOf(
            listOf(Cell(0, 0, 'a'), Cell(0, 1, 'l')),
            listOf(Cell(1, 0, 'i'))
        )
        val dict = TrieWordDictionary(listOf("ali"))
        var threw = false
        try {
            counter.count(ragged, dict)
        } catch (e: IllegalArgumentException) {
            threw = true
        }
        assertTrue("Dikdörtgen olmayan grid IllegalArgumentException fırlatmalı", threw)
    }

    // --- Küçük regresyon ---------------------------------------------

    @Test
    fun `harfler buyuk harfle verilse bile turkce lowercase ile eslesir`() {
        // Not: Türkçe locale'de 'I'.lowercase() = 'ı' olduğu için sözlük
        // karşılığı da "alı" olmalıdır. Bu test özellikle normalizasyonun
        // iki tarafta (grid ve sözlük) tutarlı uygulandığını doğrular.
        val b = board(
            "ALI",
            "XXX",
            "XXX"
        )
        val dict = TrieWordDictionary(listOf("alı"))
        assertEquals(1, counter.count(b, dict))
    }

    @Test
    fun `hicbir kelime yoksa 0 doner`() {
        val b = board(
            "qwx",
            "ytz",
            "pmn"
        )
        val dict = TrieWordDictionary(listOf("ali", "kalem", "aslan"))
        assertFalse(counter.analyze(b, dict).words.isNotEmpty())
        assertEquals(0, counter.count(b, dict))
    }
}
