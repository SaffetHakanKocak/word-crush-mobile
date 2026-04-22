package com.saffet.wordcrushmobile.domain.model

/**
 * Oyuncunun başarıyla oynadığı (onayladığı) bir kelimeyi ve o kelimeyle
 * kazanılan skoru temsil eder.
 *
 * Onaylandıktan sonra [GameState.foundWords] listesine eklenir; hem oyun
 * sırasında bulunan kelimeleri göstermek, hem de oyun sonunda [ScoreSummary]
 * oluşturmak için kullanılır.
 *
 * @property word   Oynanan kelimenin metni.
 * @property score  Bu kelimenin (özel hücre çarpanları dahil) kazandırdığı puan.
 * @property cells  Kelimeyi oluşturan hücrelerin sırasıyla referansları.
 */
data class PlayedWord(
    val word: String,
    val score: Int,
    val cells: List<Cell>
)
