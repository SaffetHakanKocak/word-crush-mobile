package com.saffet.wordcrushmobile.viewmodel

import com.saffet.wordcrushmobile.domain.engine.BoardPosition
import com.saffet.wordcrushmobile.domain.model.Cell
import com.saffet.wordcrushmobile.domain.model.JokerType
import com.saffet.wordcrushmobile.domain.model.PlayedWord

/**
 * [GameViewModel]'in UI'a yaydığı tek kaynaklı (single source of truth) state.
 *
 * Immutable data class'tır; her güncelleme `copy(...)` ile yeni bir örnek
 * üretir. Böylece Compose recomposition'ı optimum çalışır ve eşzamanlılık
 * sorunları oluşmaz.
 *
 * @property board             Grid matrisi. Boyut konfigürasyondan gelir.
 * @property selectedCells     Kullanıcının oluşturmakta olduğu seçim zinciri.
 *                             Sıralıdır; ilk eleman → ilk seçilen hücre.
 * @property currentWord       [selectedCells] harflerinden üretilen kelime.
 * @property score             Toplam skor.
 * @property remainingMoves    Kalan hamle sayısı. 0'a inince oyun biter.
 * @property availableWordCount Tahtada bulunabilecek kelime sayısı
 *                              (ileride hesaplanacak; ilk sürümde 0).
 * @property foundWords        Oyun boyunca onaylanmış kelimeler.
 * @property isGameOver        Oyun bitti mi (hamle tükendi veya manuel bitti).
 * @property isDictionaryReady Sözlük belleğe yüklendi mi.
 *                             `false` iken "Onayla" butonu beklemede.
 * @property isBoardReady      Tahta kullanıcıya gösterilmeye hazır mı.
 *                             Başlangıçta ve restart sonrası, en az bir
 *                             kelime garantilenene kadar `false` kalır.
 * @property lastMessage       UI'da geçici olarak gösterilecek son geri
 *                             bildirim (kelime kabul edildi / reddedildi vs).
 *                             `null` ise mesaj yok.
 */
data class GameUiState(
    val board: List<List<Cell>> = emptyList(),
    val selectedCells: List<Cell> = emptyList(),
    val currentWord: String = "",
    val score: Int = 0,
    val remainingMoves: Int = 0,
    val availableWordCount: Int = 0,
    val foundWords: List<PlayedWord> = emptyList(),
    val isGameOver: Boolean = false,
    val isDictionaryReady: Boolean = false,
    val isBoardReady: Boolean = false,
    val lastMessage: String? = null,
    /**
     * Envanter: her joker tipi için oyuncunun sahip olduğu adet. UI
     * alt çubuğunda buton sayacı olarak gösterilir; 0 ise buton pasif olur.
     */
    val jokerInventory: Map<JokerType, Int> = emptyMap(),
    /**
     * Aktif "hedef seçme" modunun durumu. `null` ise oyuncu kelime
     * oluşturuyor — hücre tıklamaları seçime eklenir. Aktifse hücre
     * tıklamaları joker hedefine yönlendirilir.
     */
    val jokerTargeting: JokerTargetingState? = null,
    /**
     * Animasyon katmanı: "şu an patlamakta olan" hücrelerin pozisyonları.
     *
     * Kelime onayı veya joker kullanımı sırasında gravity+refill adımından
     * HEMEN ÖNCE ViewModel bu seti doldurur; 200–300 ms sonra yeni board ile
     * birlikte tekrar `emptySet()` yapar. UI bu aralıkta hücreleri kırmızı
     * flash + scale ile vurgular; böylece yok etme / patlatma olayı gözle
     * fark edilebilir hale gelir. Board hâlâ eski haliyle render edildiği
     * için pozisyonlar orijinal (silinmeden önceki) hücrelere işaret eder.
     */
    val explodingPositions: Set<BoardPosition> = emptySet()
) {
    /** UI kolaylığı: grid'in sütun sayısı (0 ise henüz board üretilmemiş). */
    val cols: Int get() = board.firstOrNull()?.size ?: 0

    /** UI kolaylığı: grid'in satır sayısı. */
    val rows: Int get() = board.size

    /** UI kolaylığı: belirli bir pozisyon seçimde var mı? */
    fun isSelected(row: Int, col: Int): Boolean =
        selectedCells.any { it.row == row && it.col == col }

    /** UI kolaylığı: pozisyon seçim zincirinin son elemanı mı? */
    fun isLastSelected(row: Int, col: Int): Boolean {
        val last = selectedCells.lastOrNull() ?: return false
        return last.row == row && last.col == col
    }

    /** UI kolaylığı: hücre şu an joker hedefi olarak seçilmiş mi? */
    fun isJokerTarget(row: Int, col: Int): Boolean =
        jokerTargeting?.pickedTargets?.any { it.row == row && it.col == col } == true

    /**
     * UI kolaylığı: hücre şu an bir "patlatma" animasyonunun ortasında mı?
     * [explodingPositions] dolu olduğu kısa süre (200–300 ms) için `true`
     * döner; animasyon bitince set boşaltılır.
     */
    fun isExploding(row: Int, col: Int): Boolean =
        explodingPositions.any { it.row == row && it.col == col }
}

/**
 * Joker hedef seçme modunun anlık durumu.
 *
 * @property type            Aktif joker tipi.
 * @property neededTargets   Jokerin toplaması gereken hedef sayısı
 *                           (bkz. `JokerTargetSpec`).
 * @property pickedTargets   Şu ana kadar oyuncunun tıkladığı hedefler.
 * @property requiresAdjacent `true` ise ardışık hedefler komşu olmalı
 *                           (FREE_SWAP).
 */
data class JokerTargetingState(
    val type: JokerType,
    val neededTargets: Int,
    val pickedTargets: List<BoardPosition> = emptyList(),
    val requiresAdjacent: Boolean = false
) {
    val isComplete: Boolean get() = pickedTargets.size >= neededTargets
}
