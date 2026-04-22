package com.saffet.wordcrushmobile.domain.dictionary

import java.util.Locale

/**
 * Türkçe metin normalizasyonu için tekil yardımcı.
 *
 * Türkçe karakterlerin (özellikle İ/I ve i/ı çiftleri) doğru büyük-küçük
 * harf dönüşümü için `Locale("tr")` kullanılır. Aksi hâlde varsayılan
 * ROOT/İngilizce locale, "İSTANBUL".lowercase() → "i̇stanbul" gibi beklenmedik
 * dönüşümlere sebep olabilir.
 *
 * Saf Kotlin'dir (Android bağımlılığı yok), hem yükleme sırasında kelime
 * listesinde hem de kullanıcıdan gelen kelime sorgusunda aynı normalizasyonun
 * uygulanması kritiktir — bu sayede "İstanbul" ve "istanbul" aynı kelimeye
 * karşılık gelir.
 */
object TurkishTextNormalizer {

    // Not: `Locale("tr", "TR")` constructor'ı Java 19'da deprecate edildi.
    // BCP-47 etiket tabanlı forLanguageTag hem modern hem tüm Android sürümlerinde
    // geriye uyumludur.
    private val TURKISH: Locale = Locale.forLanguageTag("tr-TR")

    /** Metni ortadan kırpıp Türkçe locale ile lowercase eder. */
    fun normalize(text: String): String =
        text.trim().lowercase(TURKISH)

    /** Bir kelimenin normalize edilmiş hâli, sözlüğe uygun mu (boş değil mi)? */
    fun isNormalizedNonEmpty(text: String): Boolean =
        normalize(text).isNotEmpty()
}
