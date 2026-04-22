package com.saffet.wordcrushmobile.domain.engine

import com.saffet.wordcrushmobile.domain.model.Cell

/**
 * Bir seçim işleminin (hücre ekleme veya final kelime doğrulama) sonucu.
 *
 * Sealed interface kullanılarak "başarılı veya başarısız" iki durum tip
 * güvenli biçimde modellenir; çağıran taraf `when` ile eksiksiz (exhaustive)
 * biçimde her iki dalı ele alabilir.
 *
 * @see SelectionError başarısızlık sebepleri için.
 */
sealed interface SelectionResult {

    /**
     * Seçim kabul edildi.
     *
     * @property selection Güncel (ya da doğrulanmış) hücre zinciri.
     * @property word      Hücrelerin harflerinden oluşturulmuş kelime metni.
     */
    data class Accepted(
        val selection: List<Cell>,
        val word: String
    ) : SelectionResult

    /**
     * Seçim reddedildi.
     *
     * @property reason Reddedilme sebebi; UI tarafında kullanıcıya
     *                  uygun bir geri bildirim göstermek için kullanılır.
     */
    data class Rejected(
        val reason: SelectionError
    ) : SelectionResult
}

/**
 * Bir seçimin neden reddedildiğini açıklayan kategoriler.
 *
 * - [CELL_ALREADY_SELECTED] Aynı hücre (aynı satır/sütun) mevcut seçimde zaten var.
 * - [NOT_NEIGHBOR]          Yeni hücre, seçimin son hücresine 8 yönlü komşu değil.
 * - [TOO_SHORT]             Seçim, minimum kelime uzunluğunu sağlamıyor
 *                           (şartname gereği en az 3 harf).
 * - [EMPTY_SELECTION]       Hiç hücre seçilmemiş; submit edilecek bir seçim yok.
 * - [BROKEN_CHAIN]          Seçim zinciri içinde ardışık bir çift komşu değil
 *                           (tutarsız bir liste submit edildi).
 */
enum class SelectionError {
    CELL_ALREADY_SELECTED,
    NOT_NEIGHBOR,
    TOO_SHORT,
    EMPTY_SELECTION,
    BROKEN_CHAIN
}
