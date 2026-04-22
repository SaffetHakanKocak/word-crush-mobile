package com.saffet.wordcrushmobile.domain.model

/**
 * Oyuncunun Market'ten satın alıp oyun sırasında kullanabileceği joker türleri.
 *
 * İsim/açıklama/altın maliyeti PDF şartnamesinin §Jokerler tablosuyla birebir
 * eşleşir. Tek kaynak burası olsun diye display metadata (isim, açıklama,
 * maliyet) enum'un içine gömülmüştür; UI katmanı ayrıca string resource'a
 * ihtiyaç duymaz, Market ve Oyun ekranları aynı değerleri okur.
 *
 * @property displayName Kullanıcıya Türkçe olarak gösterilen ad.
 * @property description Kısa etki açıklaması.
 * @property costGold    Market satın alma fiyatı (altın).
 */
enum class JokerType(
    val displayName: String,
    val description: String,
    val costGold: Int
) {
    FISH(
        displayName = "Balık",
        description = "Grid üzerinde rastgele harfleri yok eder; " +
            "üstteki harfler aşağı düşer, üstten yenileri gelir.",
        costGold = 100
    ),
    WHEEL(
        displayName = "Tekerlek",
        description = "Seçtiğin harfin bulunduğu satır ve sütunu tamamen yok eder.",
        costGold = 200
    ),
    LOLLIPOP_HAMMER(
        displayName = "Lolipop Kırıcı",
        description = "İstediğin tek bir harfi grid'den siler.",
        costGold = 75
    ),
    FREE_SWAP(
        displayName = "Serbest Değiştirme",
        description = "Bitişik iki harfin yerini değiştirir.",
        costGold = 125
    ),
    LETTER_SHUFFLE(
        displayName = "Harf Karıştırma",
        description = "Grid'deki tüm harfleri yeniden karıştırır.",
        costGold = 300
    ),
    PARTY_BOOSTER(
        displayName = "Parti Güçlendiricisi",
        description = "Grid'deki tüm harfleri yok eder; yerine yenileri düşer.",
        costGold = 400
    );
}
