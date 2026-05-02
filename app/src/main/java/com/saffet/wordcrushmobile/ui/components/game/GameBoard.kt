package com.saffet.wordcrushmobile.ui.components.game

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.saffet.wordcrushmobile.domain.engine.BoardPosition
import com.saffet.wordcrushmobile.domain.model.Cell
import com.saffet.wordcrushmobile.domain.model.JokerType
import com.saffet.wordcrushmobile.domain.model.SpecialType
import com.saffet.wordcrushmobile.viewmodel.GravityAnimationState
import com.saffet.wordcrushmobile.viewmodel.JokerEffectState
import com.saffet.wordcrushmobile.viewmodel.SpecialEffectState
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

@Composable
fun GameBoard(
    board: List<List<Cell>>,
    isSelected: (row: Int, col: Int) -> Boolean,
    isLastSelected: (row: Int, col: Int) -> Boolean,
    onCellClick: (Cell) -> Unit,
    modifier: Modifier = Modifier,
    isJokerTarget: (row: Int, col: Int) -> Boolean = { _, _ -> false },
    isExploding: (row: Int, col: Int) -> Boolean = { _, _ -> false },
    jokerEffect: JokerEffectState? = null,
    specialEffects: List<SpecialEffectState> = emptyList(),
    gravityAnimation: GravityAnimationState? = null,
    enableDrag: Boolean = true,
    onDragStartCell: (row: Int, col: Int) -> Unit = { _, _ -> },
    onDragOverCell: (row: Int, col: Int) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {}
) {
    if (board.isEmpty()) return
    val rows = board.size
    val cols = board.first().size

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val gap = 6.dp
        val totalGapWidth = gap * (cols - 1)
        val cellSize = (maxWidth - totalGapWidth) / cols
        val boardHeight = cellSize * rows + gap * (rows - 1)

        val density = LocalDensity.current
        val cellPx = with(density) { cellSize.toPx() }
        val gapPx = with(density) { gap.toPx() }
        val stridePx = cellPx + gapPx
        val effect = jokerEffect

        fun toRowCol(offset: Offset): Pair<Int, Int>? {
            if (stridePx <= 0f || offset.x < 0f || offset.y < 0f) return null
            val colIdx = (offset.x / stridePx).toInt()
            val rowIdx = (offset.y / stridePx).toInt()
            if (rowIdx !in 0 until rows || colIdx !in 0 until cols) return null
            val localX = offset.x - colIdx * stridePx
            val localY = offset.y - rowIdx * stridePx
            if (localX > cellPx || localY > cellPx) return null
            return rowIdx to colIdx
        }

        val dragModifier = if (enableDrag) {
            Modifier.pointerInput(cellPx, gapPx, rows, cols, enableDrag) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val startRc = toRowCol(down.position) ?: return@awaitEachGesture
                    onDragStartCell(startRc.first, startRc.second)
                    down.consume()

                    var lastRc = startRc
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) {
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

        Box(
            modifier = dragModifier.size(width = maxWidth, height = boardHeight)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                for (r in 0 until rows) {
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        for (c in 0 until cols) {
                            val cell = board[r][c]
                            val motion = gravityAnimation?.motions?.get(BoardPosition(r, c))
                            val effectOffset = jokerEffectOffset(
                                row = r,
                                col = c,
                                rows = rows,
                                cols = cols,
                                stridePx = stridePx,
                                effect = effect
                            )
                            BoardCell(
                                letter = cell.letter,
                                isSelected = isSelected(r, c),
                                isLast = isLastSelected(r, c),
                                onClick = { onCellClick(cell) },
                                modifier = Modifier.size(cellSize),
                                isJokerTarget = isJokerTarget(r, c),
                                isExploding = isExploding(r, c),
                                isJokerEffect = effect?.isAffected(r, c) == true,
                                effectTranslationX = effectOffset.first,
                                effectTranslationY = effectOffset.second,
                                gravityInitialOffsetY = (motion?.initialOffsetRows ?: 0f) * stridePx,
                                gravityDistanceRows = motion?.distanceRows ?: 0,
                                gravityAnimationId = gravityAnimation?.id ?: 0L,
                                isNewFromGravity = motion?.isNewLetter == true,
                                special = cell.special,
                                clickable = !enableDrag
                            )
                        }
                        Spacer(Modifier.width(0.dp))
                    }
                }
            }

            BoardEffectOverlay(
                rows = rows,
                cols = cols,
                cellPx = cellPx,
                gapPx = gapPx,
                jokerEffect = effect,
                specialEffects = specialEffects,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun BoardEffectOverlay(
    rows: Int,
    cols: Int,
    cellPx: Float,
    gapPx: Float,
    jokerEffect: JokerEffectState?,
    specialEffects: List<SpecialEffectState>,
    modifier: Modifier = Modifier
) {
    val active = jokerEffect != null || specialEffects.isNotEmpty()
    val progress by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = tween(durationMillis = BOARD_EFFECT_MS, easing = FastOutSlowInEasing),
        label = "boardEffectProgress"
    )
    if (progress <= 0.01f && !active) return

    Canvas(modifier = modifier) {
        val stride = cellPx + gapPx
        val boardWidth = cols * cellPx + (cols - 1) * gapPx
        val boardHeight = rows * cellPx + (rows - 1) * gapPx
        val corner = cellPx * 0.22f

        fun topLeft(pos: BoardPosition): Offset =
            Offset(pos.col * stride, pos.row * stride)

        fun center(pos: BoardPosition): Offset =
            topLeft(pos) + Offset(cellPx / 2f, cellPx / 2f)

        fun drawCellGlow(pos: BoardPosition, color: Color, alpha: Float) {
            if (pos.row !in 0 until rows || pos.col !in 0 until cols) return
            drawRoundRect(
                color = color.copy(alpha = alpha),
                topLeft = topLeft(pos),
                size = Size(cellPx, cellPx),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner)
            )
            drawRoundRect(
                color = color.copy(alpha = alpha * 1.4f),
                topLeft = topLeft(pos),
                size = Size(cellPx, cellPx),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
                style = Stroke(width = max(2f, cellPx * 0.045f))
            )
        }

        jokerEffect?.let { effect ->
            val jokerColor = Color(0xFF7C4DFF)
            val alpha = 0.10f + 0.18f * (1f - abs(progress - 0.55f))
            val affected = if (effect.wholeBoard) {
                buildSet {
                    for (row in 0 until rows) {
                        for (col in 0 until cols) add(BoardPosition(row, col))
                    }
                }
            } else {
                effect.affectedPositions
            }
            affected.forEach { drawCellGlow(it, jokerColor, alpha) }

            if (effect.wholeBoard) {
                val sweepX = boardWidth * progress
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to Color.Transparent,
                        0.5f to jokerColor.copy(alpha = 0.32f),
                        1f to Color.Transparent,
                        startX = sweepX - cellPx,
                        endX = sweepX + cellPx
                    ),
                    topLeft = Offset.Zero,
                    size = Size(boardWidth, boardHeight)
                )
            }
        }

        specialEffects.forEach { effect ->
            val color = colorFor(effect.type)
            val alpha = 0.16f + 0.20f * (1f - abs(progress - 0.45f))
            effect.affectedPositions.forEach { drawCellGlow(it, color, alpha) }

            when (effect.type) {
                SpecialType.ROW_CLEAR -> {
                    val y = effect.origin.row * stride + cellPx / 2f
                    drawLine(
                        color = color.copy(alpha = 0.85f),
                        start = Offset(0f, y),
                        end = Offset(boardWidth * progress, y),
                        strokeWidth = cellPx * 0.16f,
                        cap = StrokeCap.Round
                    )
                }
                SpecialType.COLUMN_CLEAR -> {
                    val x = effect.origin.col * stride + cellPx / 2f
                    drawLine(
                        color = color.copy(alpha = 0.85f),
                        start = Offset(x, 0f),
                        end = Offset(x, boardHeight * progress),
                        strokeWidth = cellPx * 0.16f,
                        cap = StrokeCap.Round
                    )
                }
                SpecialType.AREA_BLAST,
                SpecialType.MEGA_BLAST -> {
                    val radius = if (effect.type == SpecialType.AREA_BLAST) 1.55f else 2.65f
                    drawCircle(
                        color = color.copy(alpha = 0.28f * (1f - progress * 0.35f)),
                        radius = cellPx * radius * progress,
                        center = center(effect.origin),
                        style = Stroke(width = cellPx * 0.10f)
                    )
                    drawCircle(
                        color = color.copy(alpha = 0.18f),
                        radius = cellPx * radius * 0.74f * progress,
                        center = center(effect.origin)
                    )
                }
                SpecialType.NONE -> Unit
            }
        }
    }
}

private fun colorFor(type: SpecialType): Color = when (type) {
    SpecialType.ROW_CLEAR -> Color(0xFF00A7B5)
    SpecialType.COLUMN_CLEAR -> Color(0xFFFFB300)
    SpecialType.AREA_BLAST -> Color(0xFFFF7043)
    SpecialType.MEGA_BLAST -> Color(0xFFE040FB)
    SpecialType.NONE -> Color.Transparent
}

private fun jokerEffectOffset(
    row: Int,
    col: Int,
    rows: Int,
    cols: Int,
    stridePx: Float,
    effect: JokerEffectState?
): Pair<Float, Float> {
    if (effect == null) return 0f to 0f

    effect.swapped?.let { (from, to) ->
        if (from.row == row && from.col == col) {
            return ((to.col - from.col) * stridePx) to ((to.row - from.row) * stridePx)
        }
        if (to.row == row && to.col == col) {
            return ((from.col - to.col) * stridePx) to ((from.row - to.row) * stridePx)
        }
    }

    if (effect.type == JokerType.LETTER_SHUFFLE && effect.wholeBoard) {
        val centerRow = (rows - 1) / 2f
        val centerCol = (cols - 1) / 2f
        val rawX = col - centerCol
        val rawY = row - centerRow
        val seededX = (((row * 31 + col * 17) % 5) - 2) * 0.08f
        val seededY = (((row * 19 + col * 23) % 5) - 2) * 0.08f
        val dirX = if (abs(rawX) < 0.01f) seededX.signOrFallback(1f) else sign(rawX)
        val dirY = if (abs(rawY) < 0.01f) seededY.signOrFallback(-1f) else sign(rawY)
        val distance = stridePx * 0.28f
        return (dirX * distance + seededX * stridePx) to
            (dirY * distance + seededY * stridePx)
    }

    return 0f to 0f
}

private fun Float.signOrFallback(fallback: Float): Float =
    if (this == 0f) fallback else sign(this)

private const val BOARD_EFFECT_MS: Int = 360
