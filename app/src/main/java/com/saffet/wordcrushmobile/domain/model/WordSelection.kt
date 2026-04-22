package com.saffet.wordcrushmobile.domain.model

/**
 * Kullanıcının o an tahtada parmağıyla/tıklamayla oluşturmakta olduğu
 * geçici kelime seçimi.
 *
 * Henüz onaylanmamış (kelime olarak oynanmamış) seçimler için kullanılır.
 * Onaylandığında bu nesne [PlayedWord]'e dönüştürülüp [GameState.foundWords]
 * listesine eklenir.
 *
 * @property cells  Seçim sırasına göre hücreler. Listenin sırası, harflerin
 *                  kelime içindeki sırasını belirler.
 */
data class WordSelection(
    val cells: List<Cell> = emptyList()
) {
    /** Seçimin oluşturduğu kelime metni. */
    val word: String
        get() = cells.map { it.letter }.joinToString(separator = "")

    /** Seçimdeki hücre (dolayısıyla harf) sayısı. */
    val length: Int
        get() = cells.size

    /** Herhangi bir hücre seçilmiş mi? */
    val isEmpty: Boolean
        get() = cells.isEmpty()
}
