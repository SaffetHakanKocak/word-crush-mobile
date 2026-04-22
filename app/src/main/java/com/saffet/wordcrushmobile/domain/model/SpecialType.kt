package com.saffet.wordcrushmobile.domain.model

/**
 * Tahtadaki bir hücrenin sahip olabileceği özel güç tipleri.
 *
 * PDF §6 "Harf Patlatma Mekaniği" ile hizalıdır:
 *  - Yeterince uzun bir kelime oluşturulduğunda, **son harfin** bulunduğu
 *    konumda normal harf yerine bir özel simge bırakılır. Oyuncu o hücreyi
 *    daha sonra bir kelimede tekrar kullanırsa, ilgili özel güç aktive olur.
 *
 * Uzunluk → Tip eşlemesi için [PowerUpRule] kullanılır.
 * Aktive olduğunda etkilenen hücrelerin hesaplanması için
 * [com.saffet.wordcrushmobile.domain.engine.PowerUpResolver] kullanılır.
 */
enum class SpecialType {
    /** Normal hücre; herhangi bir özel güç taşımaz. */
    NONE,

    /** 4 harfli kelime → Satır Temizleme. Etki: bulunduğu satırın tamamı. */
    ROW_CLEAR,

    /** 5 harfli kelime → Alan Patlatma. Etki: 3x3 çevre (8-komşu + kendisi). */
    AREA_BLAST,

    /** 6 harfli kelime → Sütun Temizleme. Etki: bulunduğu sütunun tamamı. */
    COLUMN_CLEAR,

    /** 7+ harfli kelime → Mega Patlatma. Etki: 2 birim yarıçap (5x5 alan). */
    MEGA_BLAST
}
