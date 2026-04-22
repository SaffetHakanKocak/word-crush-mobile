package com.saffet.wordcrushmobile.domain.dictionary

/**
 * [WordDictionary]'nin Trie (ön-ek ağacı) tabanlı implementasyonu.
 *
 * Amaç:
 *  - [contains] hâlâ O(k) (k = kelime uzunluğu) hızında çalışır.
 *  - [hasPrefix] artık "her zaman true" değildir; gerçek bir cevap döner.
 *    Bu, DFS tabanlı algoritmalarda (bkz.
 *    [com.saffet.wordcrushmobile.domain.engine.AvailableWordCounter]) prefix
 *    pruning yapılabilmesini sağlar: imkânsız bir öneke yol almadan geri
 *    dönülür, arama uzayı üstel ölçüde küçülür.
 *
 * Bellek:
 *  - Düğümler [Char] → [TrieNode] eşlemesi için [HashMap] kullanır.
 *    Türkçe alfabe için dense bir dizi de yapılabilirdi, ancak HashMap hem
 *    esnek (aksansız/unicode uyumlu) hem de pratikte yeterince hızlıdır.
 *  - Her düğüm `isWord` bayrağı ile bir kelimenin sonunu işaretler.
 *
 * Thread-safety:
 *  - Sınıf **immutable** (inşa sonrası değişmez) bir sözlük olarak
 *    kullanılır. `add` yalnızca constructor içinden çağrılır; okuma
 *    operasyonları paralel coroutine'lerden çağrılabilir.
 */
class TrieWordDictionary(
    words: Iterable<String>
) : WordDictionary {

    private val root = TrieNode()

    private var count: Int = 0

    init {
        for (word in words) {
            if (word.isEmpty()) continue
            if (add(word)) {
                count++
            }
        }
    }

    override val size: Int
        get() = count

    override fun contains(word: String): Boolean {
        if (word.isEmpty()) return false
        val node = traverse(word) ?: return false
        return node.isWord
    }

    override fun hasPrefix(prefix: String): Boolean {
        if (prefix.isEmpty()) return size > 0
        return traverse(prefix) != null
    }

    // --- iç yapı -------------------------------------------------------

    /**
     * Kelimeyi ağaca ekler. Tekrar eklenen kelime için `false` döner
     * (boyut sayımı tekrarlanmaz).
     */
    private fun add(word: String): Boolean {
        var node = root
        for (ch in word) {
            node = node.children.getOrPut(ch) { TrieNode() }
        }
        if (node.isWord) return false
        node.isWord = true
        return true
    }

    /**
     * [key]'i kök'ten takip eder. Herhangi bir adımda eşleşme yoksa
     * `null` döner. Bulunursa son düğümü verir — çağıran `isWord`
     * veya "bir prefix mi" sorusuna kendi karar verir.
     */
    private fun traverse(key: String): TrieNode? {
        var node: TrieNode = root
        for (ch in key) {
            node = node.children[ch] ?: return null
        }
        return node
    }

    private class TrieNode(
        val children: HashMap<Char, TrieNode> = HashMap(),
        var isWord: Boolean = false
    )
}
