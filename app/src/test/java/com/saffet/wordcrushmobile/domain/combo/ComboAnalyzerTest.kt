package com.saffet.wordcrushmobile.domain.combo

import com.saffet.wordcrushmobile.domain.dictionary.DictionaryRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * [ComboAnalyzer] için saf JVM unit testleri.
 *
 * Android'e ya da coroutines-test bağımlılığına gerek duymaz; suspend
 * fonksiyonlar [runBlocking] ile çağrılır. Sözlük yerine basit bir küme
 * tabanlı sahte implementasyon kullanılır.
 */
class ComboAnalyzerTest {

    private val locale = Locale("tr", "TR")

    // --- Fake dictionary ---------------------------------------------------

    /**
     * Testlerde kullanılan sahte sözlük. Verilen kelime kümesini büyük harfe
     * normalize edilmiş hâlde tutar; [contains] normalize edilmiş karşılaştırma
     * yapar. [preload]/[isReady] gerçek davranışı taklit eder.
     */
    private class FakeDictionary(words: Set<String>) : DictionaryRepository {
        private val set: Set<String> = words
            .map { it.uppercase(Locale("tr", "TR")) }
            .toHashSet()

        override fun isReady(): Boolean = true
        override suspend fun preload() = Unit
        override suspend fun contains(word: String): Boolean =
            set.contains(word.uppercase(Locale("tr", "TR")))

        override suspend fun hasPrefix(prefix: String): Boolean {
            val key = prefix.uppercase(Locale("tr", "TR"))
            return set.any { it.startsWith(key) }
        }

        override suspend fun snapshot(): com.saffet.wordcrushmobile.domain.dictionary.WordDictionary =
            object : com.saffet.wordcrushmobile.domain.dictionary.WordDictionary {
                override val size: Int = set.size
                override fun contains(word: String): Boolean =
                    set.contains(word.uppercase(Locale("tr", "TR")))
                override fun hasPrefix(prefix: String): Boolean {
                    val key = prefix.uppercase(Locale("tr", "TR"))
                    return set.any { it.startsWith(key) }
                }
            }
    }

    // --- extractCandidates (sözlükten bağımsız) ----------------------------

    @Test
    fun `extractCandidates - 3 harf altı için boş liste`() {
        val analyzer = ComboAnalyzer(FakeDictionary(emptySet()))
        assertEquals(emptyList<String>(), analyzer.extractCandidates(""))
        assertEquals(emptyList<String>(), analyzer.extractCandidates("AB"))
    }

    @Test
    fun `extractCandidates - 3 harflik kelimede ana kelime çıkarılır`() {
        val analyzer = ComboAnalyzer(FakeDictionary(emptySet()))
        // "ANA" için tek substring var (kendisi); ana kelime eleniyor → boş.
        assertEquals(emptyList<String>(), analyzer.extractCandidates("ANA"))
    }

    @Test
    fun `extractCandidates - ADANA için PDF orneğindeki alt kelimeler var`() {
        val analyzer = ComboAnalyzer(FakeDictionary(emptySet()))
        val candidates = analyzer.extractCandidates("ADANA")

        // Sözlükte olup olmamasından bağımsız, yalnız üretim kontrolü.
        assertTrue("ADA üretilmeli", candidates.contains("ADA"))
        assertTrue("DAN üretilmeli", candidates.contains("DAN"))
        assertTrue("ANA üretilmeli", candidates.contains("ANA"))
        assertTrue("ADAN üretilmeli", candidates.contains("ADAN"))
        assertTrue("DANA üretilmeli", candidates.contains("DANA"))
        // Ana kelime listede olmamalı.
        assertFalse("Ana kelime çıkarılmalı", candidates.contains("ADANA"))
    }

    @Test
    fun `extractCandidates - aynı substring iki kez yer almaz`() {
        val analyzer = ComboAnalyzer(FakeDictionary(emptySet()))
        // "ABABA" → "ABA" iki kez görünür (0..2 ve 2..4). Dedupe edilmeli.
        val candidates = analyzer.extractCandidates("ABABA")
        val abaCount = candidates.count { it == "ABA" }
        assertEquals(1, abaCount)
    }

    // --- analyze: sözlükle uçtan uca --------------------------------------

    @Test
    fun `analyze - PDF ADANA ornegi 4 combo uretir`() = runBlocking {
        // PDF: ADANA → {ADANA, DANA, ANA, ADA}
        val dict = FakeDictionary(setOf("ADANA", "DANA", "ANA", "ADA"))
        val result = ComboAnalyzer(dict).analyze("ADANA")

        assertEquals("ADANA", result.mainWord)
        assertEquals(setOf("DANA", "ANA", "ADA"), result.subWords.toSet())
        assertEquals(4, result.comboCount)
        assertTrue(result.isCombo)
    }

    @Test
    fun `analyze - SARI icin ARI alt kelimesi gelir`() = runBlocking {
        val dict = FakeDictionary(setOf("SARI", "ARI"))
        val result = ComboAnalyzer(dict).analyze("SARI")

        assertEquals("SARI", result.mainWord)
        assertEquals(listOf("ARI"), result.subWords)
        assertEquals(2, result.comboCount)
    }

    @Test
    fun `analyze - sozlukte olmayan alt kelimeler elenir`() = runBlocking {
        // Sadece ana kelime geçerli; alt kelimeler sözlükte yok.
        val dict = FakeDictionary(setOf("KALEM"))
        val result = ComboAnalyzer(dict).analyze("KALEM")

        assertEquals("KALEM", result.mainWord)
        assertTrue("Alt kelime bulunmamalı", result.subWords.isEmpty())
        assertEquals(1, result.comboCount)
        assertFalse(result.isCombo)
    }

    @Test
    fun `analyze - lowercase girdi buyuk harfe normalize edilir`() = runBlocking {
        val dict = FakeDictionary(setOf("SARI", "ARI"))
        val result = ComboAnalyzer(dict).analyze("sarı")

        // Türkçe locale ile "sarı".uppercase() == "SARI"
        assertEquals("SARI", result.mainWord)
        assertTrue(result.subWords.contains("ARI"))
    }

    @Test
    fun `analyze - Turkce ozel karakterlerle calisir`() = runBlocking {
        // "KIZ" substring'i var; Türkçe I/i farkını locale doğru ele almalı.
        val dict = FakeDictionary(setOf("KIZIL", "KIZ"))
        val result = ComboAnalyzer(dict).analyze("KIZIL")

        assertEquals("KIZIL", result.mainWord)
        assertTrue(result.subWords.contains("KIZ"))
    }

    @Test
    fun `analyze - ayni alt kelime iki farkli yerde de tek sayilir`() = runBlocking {
        // "ABABA" içinde "ABA" iki kez var; dedupe nedeniyle subWords'te bir kez.
        val dict = FakeDictionary(setOf("ABABA", "ABA"))
        val result = ComboAnalyzer(dict).analyze("ABABA")

        val abaCount = result.subWords.count { it == "ABA" }
        assertEquals(1, abaCount)
        assertEquals(2, result.comboCount)
    }

    @Test
    fun `analyze - bos girdi guvenli sekilde tek kelimeli sonuc dondurur`() = runBlocking {
        val dict = FakeDictionary(emptySet())
        val result = ComboAnalyzer(dict).analyze("")

        assertEquals("", result.mainWord)
        assertTrue(result.subWords.isEmpty())
        assertEquals(1, result.comboCount)
    }
}
