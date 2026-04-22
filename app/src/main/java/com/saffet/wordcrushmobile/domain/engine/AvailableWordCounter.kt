package com.saffet.wordcrushmobile.domain.engine

import com.saffet.wordcrushmobile.domain.dictionary.TurkishTextNormalizer
import com.saffet.wordcrushmobile.domain.dictionary.WordDictionary
import com.saffet.wordcrushmobile.domain.model.Cell

/**
 * Mevcut tahtada oluşturulabilecek **benzersiz** geçerli kelime sayısını
 * hesaplayan saf Kotlin servis.
 *
 * Algoritma:
 *  - Grid üzerindeki HER hücre bir başlangıç kabul edilir.
 *  - Her başlangıçtan 8 yönlü komşuluk (yukarı/aşağı/sağ/sol + 4 çapraz)
 *    ile DFS yapılır. Aynı hücre aynı yol üstünde iki kere kullanılmaz
 *    (kelime oyunları için standart kural).
 *  - Uzunluğu [MIN_WORD_LENGTH]'e ulaşan her yol [WordDictionary.contains]
 *    ile sorgulanır; geçerliyse **bir HashSet**'e eklenir. Aynı kelimeye
 *    farklı hücre yollarından varılsa bile yalnızca bir kez sayılır.
 *  - Pruning: Her uzatma adımında [WordDictionary.hasPrefix] çağrılır;
 *    "böyle bir önekle hiçbir kelime başlamıyor" cevabı gelirse o kol
 *    **derhal** kesilir. Trie tabanlı sözlükte bu O(k)'dır ve arama
 *    uzayını üstel ölçüde küçültür.
 *
 * Performans notları:
 *  - Ziyaret bilgisi `BooleanArray` üstünden `row * cols + col` flat
 *    indeks ile tutulur — Set tabanlı ziyarete göre çok daha hızlı.
 *  - [StringBuilder] tek bir kez tahsis edilir ve backtrack sırasında
 *    yalnızca uzunluğu küçültülür; milyonlarca String allocation'dan
 *    kaçınılır.
 *  - Harfler tahta üretilirken Türkçe locale'e göre normalize edilir
 *    (lowercase). Böylece sözlük anahtarlarıyla karşılaştırma tutarlıdır.
 *  - Sözlük [hasPrefix]'i `true` döndüren bir implementasyon olsa bile
 *    (örn. HashSet) algoritma **doğru** çalışır; yalnızca daha yavaştır.
 *
 * Saf Kotlin, Android bağımlılığı yok. Doğrudan JVM üzerinde test edilir.
 * [ComputeOptions] parametresi ile sınır kuralları (min/max kelime uzunluğu)
 * dışarıdan ayarlanabilir — kart boyutu büyüdüğünde maxLen ile makul bir
 * üst sınır koymak, worst-case süresini aşağıda tutar.
 */
class AvailableWordCounter {

    /**
     * [board] üzerinde oluşturulabilecek geçerli kelimeleri [dictionary]
     * üzerinden tarar ve [WordCountResult] döner.
     */
    fun analyze(
        board: List<List<Cell>>,
        dictionary: WordDictionary,
        options: ComputeOptions = ComputeOptions()
    ): WordCountResult {
        val rows = board.size
        if (rows == 0) return WordCountResult.EMPTY
        val cols = board[0].size
        if (cols == 0) return WordCountResult.EMPTY

        // Grid'i flat char dizisine kopyala; her DFS adımında normalize
        // maliyetinden kaçınmak için harfler tek seferde normalize edilir.
        val grid = CharArray(rows * cols)
        for (r in 0 until rows) {
            val row = board[r]
            require(row.size == cols) {
                "Tahta satırlarının sütun sayıları eşit olmalı (dikdörtgen grid)."
            }
            for (c in 0 until cols) {
                grid[r * cols + c] = TurkishTextNormalizer
                    .normalize(row[c].letter.toString())
                    .firstOrNull() ?: row[c].letter
            }
        }

        val visited = BooleanArray(rows * cols)
        val path = StringBuilder(options.maxLen.coerceAtLeast(options.minLen))
        val found = HashSet<String>()

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                dfs(
                    r = r,
                    c = c,
                    rows = rows,
                    cols = cols,
                    grid = grid,
                    visited = visited,
                    path = path,
                    found = found,
                    dictionary = dictionary,
                    options = options
                )
            }
        }

        return WordCountResult(
            count = found.size,
            words = found
        )
    }

    /** Geriye uyumluluk / UI kolaylığı: yalnızca sayıyı döndüren kısa yol. */
    fun count(
        board: List<List<Cell>>,
        dictionary: WordDictionary,
        options: ComputeOptions = ComputeOptions()
    ): Int = analyze(board, dictionary, options).count

    // --- DFS ----------------------------------------------------------

    private fun dfs(
        r: Int,
        c: Int,
        rows: Int,
        cols: Int,
        grid: CharArray,
        visited: BooleanArray,
        path: StringBuilder,
        found: MutableSet<String>,
        dictionary: WordDictionary,
        options: ComputeOptions
    ) {
        val idx = r * cols + c
        if (visited[idx]) return
        if (path.length >= options.maxLen) return

        path.append(grid[idx])
        visited[idx] = true

        // Prefix pruning: böyle bir önek hiçbir kelimeye evrilemiyorsa
        // derhal geri dön. Trie'da bu tek hamlede belli olur.
        val prefix = path.toString()
        if (dictionary.hasPrefix(prefix)) {
            if (path.length >= options.minLen && dictionary.contains(prefix)) {
                found.add(prefix)
            }
            // 8 yönlü komşular
            for (dr in NEIGHBOR_OFFSETS) {
                val nr = r + dr.first
                val nc = c + dr.second
                if (nr in 0 until rows && nc in 0 until cols) {
                    dfs(
                        r = nr,
                        c = nc,
                        rows = rows,
                        cols = cols,
                        grid = grid,
                        visited = visited,
                        path = path,
                        found = found,
                        dictionary = dictionary,
                        options = options
                    )
                }
            }
        }

        // backtrack
        path.setLength(path.length - 1)
        visited[idx] = false
    }

    companion object {
        /** Sayıma dahil edilecek minimum kelime uzunluğu (PDF §Oyun Kuralları). */
        const val MIN_WORD_LENGTH: Int = 3

        /**
         * Worst-case patlamayı sınırlamak için makul bir üst sınır.
         * Türkçede en uzun sözlük kelimeleri ~20 harf civarındadır; bu
         * üst limit sözlükteki tüm kelimeleri kapsar, kart büyüse bile
         * güvenli kalır.
         */
        const val DEFAULT_MAX_WORD_LENGTH: Int = 20

        /**
         * 8 yönlü komşu offsetleri (dr, dc). (0,0) hariç.
         */
        private val NEIGHBOR_OFFSETS: Array<Pair<Int, Int>> = arrayOf(
            -1 to -1, -1 to 0, -1 to 1,
            0 to -1, /* self */ 0 to 1,
            1 to -1, 1 to 0, 1 to 1
        )
    }

    /**
     * Algoritmanın çalışma sınırlarını belirleyen immutable parametreler.
     *
     * @property minLen Sayıma dahil edilecek minimum kelime uzunluğu.
     * @property maxLen DFS path'inin genişletilebileceği üst sınır.
     */
    data class ComputeOptions(
        val minLen: Int = MIN_WORD_LENGTH,
        val maxLen: Int = DEFAULT_MAX_WORD_LENGTH
    ) {
        init {
            require(minLen >= 1) { "minLen >= 1 olmalı." }
            require(maxLen >= minLen) { "maxLen >= minLen olmalı." }
        }
    }
}

/**
 * [AvailableWordCounter.analyze] çıktısı.
 *
 * @property count Bulunan benzersiz geçerli kelime sayısı.
 * @property words Bulunan benzersiz kelimelerin kendisi. UI ipuçları veya
 *                 testler için faydalıdır; domain state'i sadece [count]'u
 *                 kullanır.
 */
data class WordCountResult(
    val count: Int,
    val words: Set<String>
) {
    companion object {
        val EMPTY: WordCountResult = WordCountResult(0, emptySet())
    }
}
