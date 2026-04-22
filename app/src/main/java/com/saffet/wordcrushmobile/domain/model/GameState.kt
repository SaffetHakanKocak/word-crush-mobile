package com.saffet.wordcrushmobile.domain.model

/**
 * Oyunun herhangi bir andaki tam durumunu temsil eder.
 *
 * ViewModel bu nesneyi immutable bir state olarak tutar; her kullanıcı
 * aksiyonu (hücre seçimi, kelime onayı, joker kullanımı vb.) yeni bir
 * [GameState] kopyası üretir. Böylece hem Compose recomposition hem de
 * zaman içinde geri-alma/test senaryoları kolaylaşır.
 *
 * @property config              Oyunun başlangıç yapılandırması.
 * @property grid                Tahtanın iki boyutlu hücre matrisi
 *                               (dış liste satırlar, iç liste sütunlar).
 * @property score               Oyuncunun o ana kadar topladığı toplam puan.
 * @property remainingMoves      Kalan hamle sayısı; 0'a düşünce oyun biter.
 * @property selectedCells       Kullanıcının o anda seçmekte olduğu hücreler
 *                               (onaylanmamış kelime).
 * @property foundWords          Bu oyun boyunca başarıyla oynanmış kelimeler.
 * @property availableWordCount  Motorun tahmin ettiği, tahtada hâlâ
 *                               bulunabilecek kelime sayısı; "devam etmek
 *                               anlamlı mı" kontrolü için kullanılır.
 * @property jokers              Her joker türü için oyuncunun elindeki
 *                               kullanım hakkı sayısı.
 * @property isGameOver          Oyun bitti mi? True ise UI skor özetine geçmelidir.
 */
data class GameState(
    val config: GameConfig,
    val grid: List<List<Cell>>,
    val score: Int = 0,
    val remainingMoves: Int = config.totalMoves,
    val selectedCells: List<Cell> = emptyList(),
    val foundWords: List<PlayedWord> = emptyList(),
    val availableWordCount: Int = 0,
    val jokers: Map<JokerType, Int> = emptyMap(),
    val isGameOver: Boolean = false
)
