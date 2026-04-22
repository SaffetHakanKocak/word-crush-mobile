package com.saffet.wordcrushmobile.domain.combo

import com.saffet.wordcrushmobile.domain.dictionary.DictionaryRepository
import java.util.Locale

/**
 * Bir kelime submit edildiğinde içindeki geçerli alt kelimeleri tespit
 * eden saf domain servisi.
 *
 * PDF §Combo Mekaniği kuralları:
 *  1. Ana kelimenin içindeki **ardışık** (contiguous) substring'ler ele
 *     alınır. Örn. `ADANA` için {ADA, DAN, ANA, ADAN, DANA}.
 *  2. Minimum uzunluk: 3 harf.
 *  3. Ana kelimenin kendisi aday kümesinden çıkarılır (başka yerde ana
 *     kelime zaten dahil).
 *  4. Aday string'ler LinkedHashSet ile dedupe edilir (`ADANA → ANA x2`
 *     sorununu engeller). Çıktı sırası deterministik.
 *  5. Her aday [DictionaryRepository.contains] ile doğrulanır; sözlükte
 *     olmayanlar elenir.
 *
 * Neden contiguous? PDF örneği `ADANA → {ADANA, DANA, ANA, ADA}` tamamen
 * ardışık substring'lerle elde edilir. Subsequence (sıralı fakat aralıklı)
 * yorumu "tekrar yok" ifadesiyle daha karmaşık bir algoritma gerektirirdi;
 * örnek ve oyun mantığı ardışık yorumu destekliyor.
 *
 * Test edilebilirlik: [DictionaryRepository] arayüzü üzerinden bağımlı.
 * Unit testlerde sahte (fake) bir sözlük implementasyonu enjekte edilir.
 */
class ComboAnalyzer(
    private val dictionary: DictionaryRepository
) {

    /**
     * Verilen ana kelime için combo sonucunu hesaplar.
     *
     * Girdi boş veya [MIN_SUB_LENGTH]'ten kısaysa (veya tam olarak 3 harfse
     * alt kelime zaten olmaz) hızlı dönüş yapılır.
     */
    suspend fun analyze(mainWord: String): ComboResult {
        val normalized = mainWord.uppercase(LOCALE_TR)
        if (normalized.length <= MIN_SUB_LENGTH) {
            return ComboResult.single(normalized)
        }
        val candidates = extractCandidates(normalized)
        // Ardışık sözlük sorguları suspend fonksiyon olduğundan her iterasyon
        // ayrı coroutine noktası; listMap'i takip için `buildList` kullanıyoruz.
        val valid = buildList {
            for (candidate in candidates) {
                if (dictionary.contains(candidate)) add(candidate)
            }
        }
        return ComboResult(mainWord = normalized, subWords = valid)
    }

    /**
     * Ana kelime hariç, [MIN_SUB_LENGTH]+ harfli, ardışık, benzersiz tüm
     * substring'leri üretir. `internal` — test doğrudan çağırabilir.
     *
     * Sıralama: başlangıç indeksine göre soldan sağa, aynı indeks içinde
     * kısa → uzun. Bu sıra kullanıcının kelime içinde sol baştan okurken
     * göreceği sıraya en yakın olandır.
     */
    internal fun extractCandidates(word: String): List<String> {
        val n = word.length
        if (n < MIN_SUB_LENGTH) return emptyList()
        val unique = LinkedHashSet<String>()
        for (start in 0 until n) {
            val maxLen = n - start
            for (len in MIN_SUB_LENGTH..maxLen) {
                val sub = word.substring(start, start + len)
                if (sub == word) continue
                unique.add(sub)
            }
        }
        return unique.toList()
    }

    companion object {
        /** Combo'ya dahil edilecek alt kelimenin minimum uzunluğu. */
        const val MIN_SUB_LENGTH: Int = 3

        private val LOCALE_TR: Locale = Locale("tr", "TR")
    }
}
