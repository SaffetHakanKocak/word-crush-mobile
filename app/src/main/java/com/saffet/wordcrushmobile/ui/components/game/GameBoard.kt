package com.saffet.wordcrushmobile.ui.components.game

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.saffet.wordcrushmobile.domain.model.Cell

/**
 * Tahtayı (grid'i) çizen yüksek seviyeli bileşen.
 *
 * İki etkileşim yolu destekler:
 *  1. **Drag tabanlı seçim (varsayılan, PDF §Oyun Akışı).** `enableDrag=true`
 *     iken parent layer üzerinde bir `pointerInput` çalışır; parmak ilk
 *     temas ettiğinde [onDragStartCell], üzerinden geçtiği her yeni hücrede
 *     [onDragOverCell], kaldırıldığında [onDragEnd] (cell üzerindeyse) ya da
 *     [onDragCancel] (grid dışıysa) tetiklenir. Bu modda hücre tap'i devre
 *     dışı bırakılır, çünkü drag zaten ilk temastan itibaren seçim yönetir.
 *  2. **Tap tabanlı seçim (joker targeting için).** `enableDrag=false` iken
 *     drag layer'ı devre dışıdır; her hücre normal `onCellClick` ile çalışır.
 *     Bu, FREE_SWAP / WHEEL / LOLLIPOP gibi hedef bekleyen jokerlerde net
 *     ve kazasız seçim sağlar.
 *
 * - Grid kare olacak şekilde ekran genişliğine göre boyutlanır.
 * - Her hücre [BoardCell] ile çizilir.
 *
 * @param board            Grid matrisi.
 * @param isSelected       Verilen (row, col) seçimde mi?
 * @param isLastSelected   Verilen (row, col) seçim zincirinin sonuncusu mu?
 * @param onCellClick      Tap davranışı (yalnızca `enableDrag=false` iken
 *                         kullanılır — ör. joker targeting).
 * @param isJokerTarget    Verilen (row, col) aktif joker targeting modunda
 *                         hedef olarak seçilmiş mi?
 * @param isExploding      Verilen (row, col) şu an "patlama" animasyonunda mı?
 * @param enableDrag       Drag gesture layer'ı aktif mi (default `true`).
 * @param onDragStartCell  Parmak ilk temas ettiğinde (row, col).
 * @param onDragOverCell   Parmak yeni bir cell'e girdiğinde (row, col).
 * @param onDragEnd        Parmak bir cell üzerinde kaldırıldığında (submit).
 * @param onDragCancel     Parmak grid dışında kaldırıldığında / jest
 *                         sistem tarafından iptal edildiğinde.
 */
@Composable
fun GameBoard(
    board: List<List<Cell>>,
    isSelected: (row: Int, col: Int) -> Boolean,
    isLastSelected: (row: Int, col: Int) -> Boolean,
    onCellClick: (Cell) -> Unit,
    modifier: Modifier = Modifier,
    isJokerTarget: (row: Int, col: Int) -> Boolean = { _, _ -> false },
    isExploding: (row: Int, col: Int) -> Boolean = { _, _ -> false },
    enableDrag: Boolean = true,
    onDragStartCell: (row: Int, col: Int) -> Unit = { _, _ -> },
    onDragOverCell: (row: Int, col: Int) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {}
) {
    if (board.isEmpty()) return
    val cols = board.first().size
    val rows = board.size

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val gap = 6.dp
        val totalGapWidth = gap * (cols - 1)
        val cellSize = (maxWidth - totalGapWidth) / cols

        // Dp → px dönüşümü. pointerInput konum hesabı piksel uzayındadır.
        val density = LocalDensity.current
        val cellPx = with(density) { cellSize.toPx() }
        val gapPx = with(density) { gap.toPx() }
        val stridePx = cellPx + gapPx

        // Offset → (row, col). Cell içinde değilse (gap bölgesi veya grid
        // dışı) null döner → seçim güncellenmez, parmak gap'te dolaşırken
        // sessizce en son hücrede kalır.
        fun toRowCol(offset: Offset): Pair<Int, Int>? {
            if (stridePx <= 0f) return null
            if (offset.x < 0f || offset.y < 0f) return null
            val colIdx = (offset.x / stridePx).toInt()
            val rowIdx = (offset.y / stridePx).toInt()
            if (rowIdx !in 0 until rows || colIdx !in 0 until cols) return null
            // Gap bölgesinde mi? (her stride'ın ilk cellPx'i cell, kalan gap)
            val localX = offset.x - colIdx * stridePx
            val localY = offset.y - rowIdx * stridePx
            if (localX > cellPx || localY > cellPx) return null
            return rowIdx to colIdx
        }

        val dragModifier = if (enableDrag) {
            Modifier.pointerInput(cellPx, gapPx, rows, cols, enableDrag) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val startRc = toRowCol(down.position)
                    if (startRc == null) {
                        // İlk temas gap veya boş alanda → bu jest'i atla.
                        return@awaitEachGesture
                    }
                    onDragStartCell(startRc.first, startRc.second)
                    // Child clickable'lara gitmesin diye ilk down'ı tüket.
                    down.consume()

                    var lastRc: Pair<Int, Int> = startRc
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) {
                            // Parmak kaldırıldı.
                            val endRc = toRowCol(change.position)
                            if (endRc != null) onDragEnd() else onDragCancel()
                            change.consume()
                            break
                        }
                        val rc = toRowCol(change.position)
                        if (rc != null && rc != lastRc) {
                            onDragOverCell(rc.first, rc.second)
                            lastRc = rc
                        }
                        change.consume()
                    }
                }
            }
        } else {
            Modifier
        }

        Column(
            modifier = dragModifier,
            verticalArrangement = Arrangement.spacedBy(gap)
        ) {
            for (r in 0 until rows) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(gap)
                ) {
                    for (c in 0 until cols) {
                        val cell = board[r][c]
                        BoardCell(
                            letter = cell.letter,
                            isSelected = isSelected(r, c),
                            isLast = isLastSelected(r, c),
                            onClick = { onCellClick(cell) },
                            modifier = Modifier.size(cellSize),
                            isJokerTarget = isJokerTarget(r, c),
                            isExploding = isExploding(r, c),
                            special = cell.special,
                            // Drag aktifken tap devre dışı — tüm pointer
                            // olayları parent gesture layer'ı yönetir.
                            clickable = !enableDrag
                        )
                    }
                    Spacer(Modifier.width(0.dp))
                }
            }
        }
    }
}
