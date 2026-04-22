package com.saffet.wordcrushmobile.domain.joker

import com.saffet.wordcrushmobile.domain.engine.BoardPosition
import com.saffet.wordcrushmobile.domain.model.Cell

/**
 * [JokerEngine.apply] çıktısı. Başarılı uygulama veya hata sebebi.
 */
sealed class JokerResult {

    /**
     * Başarılı uygulama sonucu.
     *
     * @property newBoard         Güncel tahta.
     * @property removedPositions Silinen hücrelerin pozisyonları
     *                            (FREE_SWAP için boş; swap silme değildir).
     * @property swapped          FREE_SWAP'te takas edilen iki konum;
     *                            diğer jokerlerde `null`.
     */
    data class Success(
        val newBoard: List<List<Cell>>,
        val removedPositions: Set<BoardPosition> = emptySet(),
        val swapped: Pair<BoardPosition, BoardPosition>? = null
    ) : JokerResult()

    /** Hedef konum tahta dışındaysa veya gerekli komşuluk sağlanmıyorsa. */
    data class InvalidTarget(val reason: Reason) : JokerResult() {
        enum class Reason {
            OUT_OF_BOUNDS,
            TARGETS_NOT_ADJACENT,
            TARGETS_SAME_CELL,
            BOARD_EMPTY
        }
    }
}
