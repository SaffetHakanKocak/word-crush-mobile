package com.saffet.wordcrushmobile.domain.dictionary

/**
 * Sözlüğün uygulama çapında tek kaynağı.
 *
 * Domain katmanında tanımlıdır — böylece `data` katmanındaki gerçek
 * implementasyon (ör. [com.saffet.wordcrushmobile.data.repository.DefaultDictionaryRepository])
 * değiştirilebilir veya testlerde fake ile değiştirilebilir.
 *
 * Yaşam döngüsü:
 *  1. Uygulama başlarken [preload] çağrılır (genellikle `Application.onCreate`
 *     içinden bir coroutine ile).
 *  2. Yükleme tamamlanana kadar [isReady] `false` döner; bu süreçte [contains]
 *     çağrılırsa metot yüklemeyi bitirmeyi bekleyip sonucu döner (asenkron,
 *     askıda-suspend).
 *  3. Yükleme tamamlandıktan sonra [contains] çağrıları O(1) hızında
 *     tamamlanır (HashSet varsayılanıyla).
 */
interface DictionaryRepository {

    /** Sözlük bellekte hazır mı? UI'da "yükleniyor" göstergesi için kullanılabilir. */
    fun isReady(): Boolean

    /**
     * Sözlüğü belleğe yükler. Tekrar çağrılması güvenlidir; yalnızca ilk
     * çağrı gerçek yüklemeyi yapar (idempotent).
     */
    suspend fun preload()

    /**
     * Kelimenin sözlükte olup olmadığını döner.
     *
     * Çağrı, yükleme henüz bitmediyse askıda kalarak bekler; bu sayede
     * çağıran tarafta "hazır mı?" kontrolü tekrarına gerek kalmaz.
     *
     * Girdi, Türkçe locale ile normalize edilir (bkz. [TurkishTextNormalizer]).
     */
    suspend fun contains(word: String): Boolean

    /**
     * Verilen önek ile başlayan en az bir kelime var mı?
     *
     * Trie tabanlı implementasyonda O(prefix.length) sürede cevap verir.
     * HashSet tabanlı implementasyon prefix sorgusunu gerçekleştiremediği
     * için güvenli tarafta kalıp `true` döner — bu durumda arama
     * algoritmaları pruning yapamaz ama doğruluk bozulmaz.
     */
    suspend fun hasPrefix(prefix: String): Boolean

    /**
     * Belleğe yüklenmiş sözlüğün sync (non-suspend) bir görünümünü verir.
     *
     * Kullanım amacı: DFS/arama algoritmaları (bkz.
     * [com.saffet.wordcrushmobile.domain.engine.AvailableWordCounter])
     * milyonlarca sorgu yapabilir; her sorgu için suspend state-machine
     * maliyetinden kaçınmak için sözlüğün anlık bir referansı alınır ve
     * senkron olarak sorgulanır.
     *
     * Çağrı, yükleme tamamlanmamışsa askıda bekler ve hazır olunca döner.
     * Döndürülen [WordDictionary] immutable kabul edilmelidir.
     */
    suspend fun snapshot(): WordDictionary
}
