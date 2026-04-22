package com.saffet.wordcrushmobile.domain.dictionary

/**
 * Oyunun "sözlük" soyutlaması.
 *
 * Uygulamanın geri kalanı sözlük sorgularını yalnızca bu arayüz üzerinden
 * yapar. Böylece mevcut [HashSetWordDictionary] ileride performans veya
 * prefix sorguları için bir Trie implementasyonuyla değiştirilebilir;
 * çağıran tarafta hiçbir değişiklik gerekmez.
 *
 * Tüm fonksiyonlar normalize edilmiş (örn. Türkçe lowercase) girdi bekler.
 * Normalizasyondan [TurkishTextNormalizer] sorumludur ve sözlük
 * repository'si tarafından girdi yolu üzerinde uygulanır.
 */
interface WordDictionary {

    /** Sözlükte kaç kelime var. Test/teşhis amaçlı. */
    val size: Int

    /** Verilen kelime sözlükte mevcut mu? O(1) hedeflenir. */
    fun contains(word: String): Boolean

    /**
     * Verilen önek ile başlayan en az bir kelime var mı?
     *
     * Varsayılan implementasyon `true` döner — HashSet gibi prefix sorgusu
     * yapamayan yapılar için "bilmiyorum, tarafsız kal" anlamına gelir.
     * [Trie] tabanlı bir implementasyon bu metodu override ederek gerçek
     * cevabı O(prefix.length) sürede sağlayacaktır.
     *
     * Kullanım: Kullanıcı harf seçerken "bu başlangıçtan geçerli bir kelime
     * çıkabilir mi?" kontrolü (canlı ipucu / hint).
     */
    fun hasPrefix(prefix: String): Boolean = true
}
