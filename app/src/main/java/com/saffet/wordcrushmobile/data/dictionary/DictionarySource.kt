package com.saffet.wordcrushmobile.data.dictionary

/**
 * Ham kelime listesini sağlayan düşük seviyeli kaynak soyutlaması.
 *
 * Uygulama Android assets üzerinden beslenir ([AssetDictionarySource]),
 * ancak testlerde sabit bir listeyle kolayca fake edilebilir:
 *
 * ```
 * class FakeDictionarySource(private val items: List<String>) : DictionarySource {
 *     override suspend fun load(): List<String> = items
 * }
 * ```
 */
interface DictionarySource {

    /**
     * Tüm kelimeleri tek seferde bellekte döner.
     *
     * Dönen liste normalize edilmemiş olabilir; normalizasyon üst katmanda
     * (repository) yapılır. `suspend` olmasının sebebi IO işleminin
     * arkaplana alınmasına olanak vermektir.
     */
    suspend fun load(): List<String>
}
