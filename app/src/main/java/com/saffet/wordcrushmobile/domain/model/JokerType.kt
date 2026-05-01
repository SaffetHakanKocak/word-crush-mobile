package com.saffet.wordcrushmobile.domain.model

/**
 * Oyuncunun Market'ten satın alıp oyun sırasında kullanabileceği joker türleri.
 *
 * Market ve oyun ekranları aynı sunum bilgisini kullansın diye jokerin görünen
 * metadata'sı burada merkezileşir.
 */
enum class JokerType(
    val displayName: String,
    val description: String,
    val purpose: String,
    val usageMethod: String,
    val costGold: Int
) {
    FISH(
        displayName = "Balık",
        description = "Gridde rastgele olarak harfleri yok eder; yok olan harflerin üzerindeki harfler aşağı düşer.",
        purpose = "Sıkışık bölgelerde rastgele temizlik sağlayarak yeni kelime imkanları oluşturur.",
        usageMethod = "Anında kullanılır · Rastgele etki uygular",
        costGold = 100
    ),
    WHEEL(
        displayName = "Tekerlek",
        description = "Seçilen harfin bulunduğu satır ve sütundaki tüm harfleri yok eder.",
        purpose = "Tahtada geniş bir çapraz etki oluşturarak alan açar.",
        usageMethod = "Grid üzerinde 1 hücre seçilir",
        costGold = 200
    ),
    LOLLIPOP_HAMMER(
        displayName = "Lolipop Kırıcı",
        description = "Seçilen tek bir harfi yok eder; üstteki harfler aşağı düşer.",
        purpose = "Kritik tek bir hücreyi kaldırarak yeni eşleşme ve kelime fırsatı oluşturur.",
        usageMethod = "Grid üzerinde 1 hücre seçilir",
        costGold = 75
    ),
    FREE_SWAP(
        displayName = "Serbest Değiştirme",
        description = "Gridde birbirine temas eden iki harfin yer değiştirmesini sağlar.",
        purpose = "Mevcut harfleri avantajlı konuma getirerek yeni kelime oluşturmayı kolaylaştırır.",
        usageMethod = "Grid üzerinde komşu 2 hücre seçilir",
        costGold = 125
    ),
    LETTER_SHUFFLE(
        displayName = "Harf Karıştırma",
        description = "Gridde bulunan harfleri rastgele karıştırır.",
        purpose = "Tıkanmış tahtayı yeniden oynanabilir hale getirmek için kullanılır.",
        usageMethod = "Tüm grid'e uygulanır",
        costGold = 300
    ),
    PARTY_BOOSTER(
        displayName = "Parti Güçlendiricisi",
        description = "Griddeki tüm harfleri yok eder ve yukarıdan yeniden harf düşmesini sağlar.",
        purpose = "Tahtayı tamamen yenileyerek oyuncuya yeni başlangıç fırsatı verir.",
        usageMethod = "Tüm grid'e uygulanır",
        costGold = 400
    );
}
