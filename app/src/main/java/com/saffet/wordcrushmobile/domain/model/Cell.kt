package com.saffet.wordcrushmobile.domain.model

/**
 * Tahtadaki tek bir hücreyi (kare) temsil eder.
 *
 * @property row      Hücrenin satır indeksi (0 tabanlı, yukarıdan aşağı).
 * @property col      Hücrenin sütun indeksi (0 tabanlı, soldan sağa).
 * @property letter   Hücrede gösterilen harf.
 * @property special  Hücrenin özel davranış tipi (bomba, çarpan, buz vb.).
 * @property isSelected  Kullanıcının mevcut kelime seçiminde bu hücreyi dahil
 *                       edip etmediği. UI tarafında vurgu için kullanılır.
 */
data class Cell(
    val row: Int,
    val col: Int,
    val letter: Char,
    val special: SpecialType = SpecialType.NONE,
    val isSelected: Boolean = false
)
