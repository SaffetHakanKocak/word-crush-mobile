package com.saffet.wordcrushmobile.data.repository

import com.saffet.wordcrushmobile.data.dictionary.DictionarySource
import com.saffet.wordcrushmobile.domain.dictionary.DictionaryRepository
import com.saffet.wordcrushmobile.domain.dictionary.TrieWordDictionary
import com.saffet.wordcrushmobile.domain.dictionary.TurkishTextNormalizer
import com.saffet.wordcrushmobile.domain.dictionary.WordDictionary
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * [DictionaryRepository]'nin varsayılan, thread-safe implementasyonu.
 *
 * - Yükleme yalnızca bir kez yapılır ([Mutex] ile çift kontrol).
 * - Varsayılan olarak [TrieWordDictionary] üretir. Trie; hem `contains`
 *   hem de `hasPrefix` sorgularını O(k) hızında cevaplar. Bu özellikle
 *   [com.saffet.wordcrushmobile.domain.engine.AvailableWordCounter]
 *   DFS'inde prefix pruning için kritiktir. İsteyen alternatif bir
 *   implementasyon ([dictionaryFactory]) enjekte edebilir.
 * - Girdiler hem yükleme aşamasında hem de sorgu aşamasında
 *   [TurkishTextNormalizer] ile normalize edilir (tutarlılık garantisi).
 */
class DefaultDictionaryRepository(
    private val source: DictionarySource,
    private val dictionaryFactory: (Collection<String>) -> WordDictionary = { words ->
        TrieWordDictionary(words)
    }
) : DictionaryRepository {

    private val initMutex = Mutex()

    @Volatile
    private var dictionary: WordDictionary? = null

    override fun isReady(): Boolean = dictionary != null

    override suspend fun preload() {
        if (dictionary != null) return
        initMutex.withLock {
            if (dictionary != null) return
            val raw = source.load()
            val normalized = raw
                .asSequence()
                .map(TurkishTextNormalizer::normalize)
                .filter { it.isNotEmpty() }
                .toSet()
            dictionary = dictionaryFactory(normalized)
        }
    }

    override suspend fun contains(word: String): Boolean {
        val dict = ensureLoaded()
        return dict.contains(TurkishTextNormalizer.normalize(word))
    }

    override suspend fun hasPrefix(prefix: String): Boolean {
        val dict = ensureLoaded()
        return dict.hasPrefix(TurkishTextNormalizer.normalize(prefix))
    }

    override suspend fun snapshot(): WordDictionary = ensureLoaded()

    private suspend fun ensureLoaded(): WordDictionary {
        dictionary?.let { return it }
        preload()
        return dictionary!!
    }
}
