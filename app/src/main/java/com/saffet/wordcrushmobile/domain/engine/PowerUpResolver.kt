package com.saffet.wordcrushmobile.domain.engine

import com.saffet.wordcrushmobile.domain.model.Cell
import com.saffet.wordcrushmobile.domain.model.SpecialType

/**
 * Tahtadaki bir (row, col) konumunu temsil eden hafif, immutable pozisyon.
 *
 * `Pair<Int, Int>` yerine adlandırılmış bir tip tercih edilmiştir:
 *  - Okuma tarafında `p.row`/`p.col` → niyet açık.
 *  - HashSet içinde anahtar olarak kullanılabilir (equals/hashCode auto).
 *  - Engine'deki `positionKey` (Long paketleme) iç detayı dışa sızmaz.
 */
data class BoardPosition(val row: Int, val col: Int)

/**
 * Bir özel güç (örn. [SpecialType.ROW_CLEAR]) aktive olduğunda hangi
 * koordinatların etkilendiğini hesaplayan saf Kotlin servis.
 *
 * Stateless; yeniden kullanılabilir ve [applyWord] gibi yerlerde enjekte
 * edilerek test için fake ile değiştirilebilir.
 */
class PowerUpResolver {

    /**
     * [origin] hücresi bir kelimede kullanıldığında patlatılacak **ek**
     * hücrelerin pozisyon kümesini döner.
     *
     * Not: Origin'in kendisi sonuç setine dahildir — çağıran taraf
     * "kelimenin kendi harfleri" ile birleştirirken set birleşimi zaten
     * tekrarı eler. Son-hücre korumak gerekiyorsa çağıran taraf onu
     * setten çıkarır.
     *
     * [SpecialType.NONE] için boş set döner.
     */
    fun affected(
        board: List<List<Cell>>,
        origin: Cell
    ): Set<BoardPosition> {
        val rows = board.size
        if (rows == 0) return emptySet()
        val cols = board[0].size
        if (cols == 0) return emptySet()

        return when (origin.special) {
            SpecialType.NONE -> emptySet()
            SpecialType.ROW_CLEAR -> rowPositions(origin.row, cols)
            SpecialType.COLUMN_CLEAR -> columnPositions(origin.col, rows)
            SpecialType.AREA_BLAST -> squareRadius(origin.row, origin.col, rows, cols, radius = 1)
            SpecialType.MEGA_BLAST -> squareRadius(origin.row, origin.col, rows, cols, radius = 2)
        }
    }

    // --- Şekil yardımcıları ------------------------------------------

    private fun rowPositions(row: Int, cols: Int): Set<BoardPosition> {
        val out = HashSet<BoardPosition>(cols)
        for (c in 0 until cols) out.add(BoardPosition(row, c))
        return out
    }

    private fun columnPositions(col: Int, rows: Int): Set<BoardPosition> {
        val out = HashSet<BoardPosition>(rows)
        for (r in 0 until rows) out.add(BoardPosition(r, col))
        return out
    }

    /**
     * (r0, c0) merkezli, Chebyshev mesafesi ≤ [radius] olan bütün
     * koordinatları döner. radius=1 → 3x3, radius=2 → 5x5 vb.
     * Tahtanın dışında kalan indeksler atlanır.
     */
    private fun squareRadius(
        r0: Int,
        c0: Int,
        rows: Int,
        cols: Int,
        radius: Int
    ): Set<BoardPosition> {
        val side = (radius * 2 + 1)
        val out = HashSet<BoardPosition>(side * side)
        for (dr in -radius..radius) {
            val nr = r0 + dr
            if (nr !in 0 until rows) continue
            for (dc in -radius..radius) {
                val nc = c0 + dc
                if (nc !in 0 until cols) continue
                out.add(BoardPosition(nr, nc))
            }
        }
        return out
    }
}
