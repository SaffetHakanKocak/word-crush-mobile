package com.saffet.wordcrushmobile.domain.joker

import com.saffet.wordcrushmobile.domain.engine.BoardPosition
import com.saffet.wordcrushmobile.domain.engine.WordCrushEngine
import com.saffet.wordcrushmobile.domain.model.Cell
import com.saffet.wordcrushmobile.domain.model.SpecialType
import kotlin.math.abs
import kotlin.random.Random

/**
 * PDF §9 "Joker Mekaniği"ni uygulayan saf Kotlin servis.
 *
 * Her joker tipi için ayrı bir private uygulayıcı vardır; dispatch
 * [apply] içindeki `when` ile yapılır. Sealed [JokerAction] hiyerarşisi
 * sayesinde derleyici yeni bir joker eklendiğinde eksik kolları raporlar.
 *
 * Bağımlılıklar:
 *  - [WordCrushEngine]: harflerin silinmesi + yerçekimi + refill için
 *    tek kaynak; Joker'in kendisi `collapseAndRefill`'ı yeniden
 *    implement etmez.
 *  - [random]: testlerde deterministik davranış için enjekte edilir.
 *
 * Saflık: Android bağımlılığı yoktur; doğrudan JVM üzerinde test edilir.
 */
class JokerEngine(
    private val random: Random = Random.Default,
    private val engine: WordCrushEngine = WordCrushEngine(random = random)
) {

    /**
     * [action]'ı [board] üzerinde uygular ve [JokerResult] döner.
     * Hedefler tahta dışıysa veya gereken komşuluk sağlanmazsa
     * [JokerResult.InvalidTarget] döner — envanter düşürme çağıranın
     * sorumluluğundadır (başarısız işlemde yok edilmez).
     */
    fun apply(board: List<List<Cell>>, action: JokerAction): JokerResult {
        if (board.isEmpty() || board.first().isEmpty()) {
            return JokerResult.InvalidTarget(JokerResult.InvalidTarget.Reason.BOARD_EMPTY)
        }

        return when (action) {
            is JokerAction.Fish         -> applyFish(board, action)
            is JokerAction.Wheel        -> applyWheel(board, action)
            is JokerAction.Lollipop     -> applyLollipop(board, action)
            is JokerAction.FreeSwap     -> applyFreeSwap(board, action)
            JokerAction.LetterShuffle   -> applyLetterShuffle(board)
            JokerAction.PartyBooster    -> applyPartyBooster(board)
        }
    }

    // --- FISH ----------------------------------------------------------

    /**
     * Tahtadan rastgele [JokerAction.Fish.count] hücre siler; ardından
     * [WordCrushEngine.collapseAndRefill] ile yerçekimi + refill uygulanır.
     *
     * Tahta boyutundan büyük sayımlar tahta boyutuna kırpılır.
     */
    private fun applyFish(
        board: List<List<Cell>>,
        action: JokerAction.Fish
    ): JokerResult.Success {
        val rows = board.size
        val cols = board[0].size
        val totalCells = rows * cols
        val target = action.count.coerceAtMost(totalCells)

        val allPositions = ArrayList<BoardPosition>(totalCells)
        for (r in 0 until rows) for (c in 0 until cols) {
            allPositions.add(BoardPosition(r, c))
        }
        allPositions.shuffle(random)
        val removed = allPositions.take(target).toHashSet()

        val clearedList = removed.map { board[it.row][it.col] }
        val newBoard = engine.collapseAndRefill(board, clearedList)
        return JokerResult.Success(newBoard, removed)
    }

    // --- WHEEL ---------------------------------------------------------

    private fun applyWheel(
        board: List<List<Cell>>,
        action: JokerAction.Wheel
    ): JokerResult {
        if (!inBounds(board, action.target)) {
            return JokerResult.InvalidTarget(JokerResult.InvalidTarget.Reason.OUT_OF_BOUNDS)
        }
        val rows = board.size
        val cols = board[0].size
        val removed = HashSet<BoardPosition>(rows + cols)
        for (c in 0 until cols) removed.add(BoardPosition(action.target.row, c))
        for (r in 0 until rows) removed.add(BoardPosition(r, action.target.col))

        val clearedList = removed.map { board[it.row][it.col] }
        val newBoard = engine.collapseAndRefill(board, clearedList)
        return JokerResult.Success(newBoard, removed)
    }

    // --- LOLLIPOP ------------------------------------------------------

    private fun applyLollipop(
        board: List<List<Cell>>,
        action: JokerAction.Lollipop
    ): JokerResult {
        if (!inBounds(board, action.target)) {
            return JokerResult.InvalidTarget(JokerResult.InvalidTarget.Reason.OUT_OF_BOUNDS)
        }
        val removed = setOf(action.target)
        val clearedList = listOf(board[action.target.row][action.target.col])
        val newBoard = engine.collapseAndRefill(board, clearedList)
        return JokerResult.Success(newBoard, removed)
    }

    // --- FREE_SWAP -----------------------------------------------------

    /**
     * İki hücrenin içeriğini (harf + özel tip) takas eder; konumları
     * aynı kalır. [JokerAction.FreeSwap]'in `a` ve `b` hedefleri
     * 8 yönlü komşu olmak ZORUNDADIR.
     */
    private fun applyFreeSwap(
        board: List<List<Cell>>,
        action: JokerAction.FreeSwap
    ): JokerResult {
        if (!inBounds(board, action.a) || !inBounds(board, action.b)) {
            return JokerResult.InvalidTarget(JokerResult.InvalidTarget.Reason.OUT_OF_BOUNDS)
        }
        if (action.a == action.b) {
            return JokerResult.InvalidTarget(JokerResult.InvalidTarget.Reason.TARGETS_SAME_CELL)
        }
        if (!areAdjacent(action.a, action.b)) {
            return JokerResult.InvalidTarget(JokerResult.InvalidTarget.Reason.TARGETS_NOT_ADJACENT)
        }

        val rows = board.size
        val cols = board[0].size
        val cellA = board[action.a.row][action.a.col]
        val cellB = board[action.b.row][action.b.col]

        val newBoard = List(rows) { r ->
            List(cols) { c ->
                when {
                    r == action.a.row && c == action.a.col ->
                        cellA.copy(letter = cellB.letter, special = cellB.special, isSelected = false)
                    r == action.b.row && c == action.b.col ->
                        cellB.copy(letter = cellA.letter, special = cellA.special, isSelected = false)
                    else -> board[r][c]
                }
            }
        }
        return JokerResult.Success(
            newBoard = newBoard,
            removedPositions = emptySet(),
            swapped = action.a to action.b
        )
    }

    // --- LETTER_SHUFFLE ------------------------------------------------

    /**
     * Özel simge taşımayan (NONE) tüm hücrelerin harflerini rastgele
     * permüte eder. Özel simgeli hücreler yerinde kalır (PDF §6 ile
     * uyumlu) ve harfleri değişmez.
     */
    private fun applyLetterShuffle(board: List<List<Cell>>): JokerResult.Success {
        val rows = board.size
        val cols = board[0].size
        val shufflablePositions = ArrayList<BoardPosition>(rows * cols)
        val letters = ArrayList<Char>(rows * cols)

        for (r in 0 until rows) for (c in 0 until cols) {
            val cell = board[r][c]
            if (cell.special == SpecialType.NONE) {
                shufflablePositions.add(BoardPosition(r, c))
                letters.add(cell.letter)
            }
        }
        letters.shuffle(random)

        val newBoard = ArrayList<MutableList<Cell>>(rows)
        for (r in 0 until rows) {
            newBoard.add(MutableList(cols) { c -> board[r][c] })
        }
        for (i in shufflablePositions.indices) {
            val pos = shufflablePositions[i]
            newBoard[pos.row][pos.col] = board[pos.row][pos.col].copy(
                letter = letters[i],
                isSelected = false
            )
        }
        return JokerResult.Success(newBoard.map { it.toList() })
    }

    // --- PARTY_BOOSTER -------------------------------------------------

    /**
     * Tüm grid'i sıfırlar ve [WordCrushEngine.generateBoard] ile yeniden
     * üretir. Özel simgeler dâhil her şey resetlenir; PDF: "tüm harfler
     * yok edilir ve tekrardan rastgele bir şekilde harfler yukarıdan
     * aşağıya düşmektedir."
     */
    private fun applyPartyBooster(board: List<List<Cell>>): JokerResult.Success {
        val rows = board.size
        val cols = board[0].size
        val removed = HashSet<BoardPosition>(rows * cols)
        for (r in 0 until rows) for (c in 0 until cols) {
            removed.add(BoardPosition(r, c))
        }
        // generateBoard özel simgesiz, tamamen yeni bir tahta üretir.
        val newBoard = engine.generateBoard(rows = rows, cols = cols)
        return JokerResult.Success(newBoard, removed)
    }

    // --- yardımcılar ---------------------------------------------------

    private fun inBounds(board: List<List<Cell>>, p: BoardPosition): Boolean {
        val rows = board.size
        val cols = board[0].size
        return p.row in 0 until rows && p.col in 0 until cols
    }

    private fun areAdjacent(a: BoardPosition, b: BoardPosition): Boolean {
        val dr = abs(a.row - b.row)
        val dc = abs(a.col - b.col)
        return (dr + dc) > 0 && dr <= 1 && dc <= 1
    }
}
