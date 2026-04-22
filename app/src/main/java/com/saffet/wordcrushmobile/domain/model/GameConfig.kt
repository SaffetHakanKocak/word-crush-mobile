package com.saffet.wordcrushmobile.domain.model

/**
 * Bir oyunun başlangıçta belirlenen, oyun süresince değişmeyen yapılandırması.
 *
 * Zorluk seçiminden veya varsayılanlardan türetilir ve motoruna iletilir.
 *
 * @property rows           Tahta satır sayısı.
 * @property cols           Tahta sütun sayısı.
 * @property totalMoves     Oyuncuya verilen toplam hamle hakkı.
 * @property minWordLength  Geçerli sayılan en kısa kelime uzunluğu.
 */
data class GameConfig(
    val rows: Int,
    val cols: Int,
    val totalMoves: Int,
    val minWordLength: Int = 3
)
