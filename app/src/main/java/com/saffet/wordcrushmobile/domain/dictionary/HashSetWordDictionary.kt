package com.saffet.wordcrushmobile.domain.dictionary

/**
 * [WordDictionary]'nin HashSet tabanlı temel implementasyonu.
 *
 * - `contains` O(1) ortalamadır (HashSet).
 * - `hasPrefix` varsayılan olarak her zaman `true` döner (taban sınıftan gelir).
 *   Prefix kontrolü gerçekten gerekiyorsa Trie tabanlı bir implementasyon tercih
 *   edilmelidir.
 *
 * Bu sınıf saf Kotlin'dir; Android bağımlılığı yoktur ve JVM üzerinde
 * doğrudan test edilebilir.
 */
class HashSetWordDictionary(
    words: Iterable<String>
) : WordDictionary {

    private val set: Set<String> = words.toHashSet()

    override val size: Int
        get() = set.size

    override fun contains(word: String): Boolean = set.contains(word)
}
