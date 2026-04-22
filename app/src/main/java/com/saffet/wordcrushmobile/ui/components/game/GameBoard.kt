package com.saffet.wordcrushmobile.ui.components.game

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
import androidx.compose.ui.unit.dp
import com.saffet.wordcrushmobile.domain.model.Cell

/**
 * Tahtayı (grid'i) çizen yüksek seviyeli bileşen.
 *
 * - Grid kare olacak şekilde ekran genişliğine göre boyutlanır.
 * - Her hücre [BoardCell] ile çizilir.
 * - Hücre pozisyon bilgisi, seçim durumu (isSelected / isLast) ve tıklama
 *   callback'i state'ten türetilir.
 *
 * @param board          Grid matrisi.
 * @param isSelected     Verilen (row, col) seçimde mi?
 * @param isLastSelected Verilen (row, col) seçim zincirinin sonuncusu mu?
 * @param onCellClick    Hücre tıklama olayı; Cell'i iletir.
 * @param isJokerTarget  Verilen (row, col) aktif joker targeting modunda
 *                       hedef olarak seçilmiş mi? Default `false` döner →
 *                       joker özelliği yoksa mevcut davranış aynı kalır.
 */
@Composable
fun GameBoard(
    board: List<List<Cell>>,
    isSelected: (row: Int, col: Int) -> Boolean,
    isLastSelected: (row: Int, col: Int) -> Boolean,
    onCellClick: (Cell) -> Unit,
    modifier: Modifier = Modifier,
    isJokerTarget: (row: Int, col: Int) -> Boolean = { _, _ -> false }
) {
    if (board.isEmpty()) return
    val cols = board.first().size
    val rows = board.size

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Kare hücreler için kısa kenarı baz al; satır sayısı daha fazlaysa
        // yükseklik de sınırlayabilir — yine de genelde cols == rows.
        val gap = 6.dp
        val totalGapWidth = gap * (cols - 1)
        val cellSize = (maxWidth - totalGapWidth) / cols

        Column(
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
                            isJokerTarget = isJokerTarget(r, c)
                        )
                    }
                    // Satır sonu güvenliği: col sayısı değişkense hizalamayı
                    // bozmamak için sabit bir bitiş boşluğu.
                    Spacer(Modifier.width(0.dp))
                }
            }
        }
    }
}
