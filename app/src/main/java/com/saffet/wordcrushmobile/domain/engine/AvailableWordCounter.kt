package com.saffet.wordcrushmobile.domain.engine

import com.saffet.wordcrushmobile.domain.dictionary.TurkishTextNormalizer
import com.saffet.wordcrushmobile.domain.dictionary.WordDictionary
import com.saffet.wordcrushmobile.domain.model.Cell

/**
 * Mevcut tahtada ortak hucre kullanmadan secilebilecek gecerli kelime sayisini
 * hizli yaklasik cozumle hesaplayan saf Kotlin servis.
 *
 * Akis:
 *  1. Grid uzerindeki gecerli kelime adaylari, kullandiklari hucre maskesiyle
 *     birlikte taranir.
 *  2. Ayni kelimenin path sayisi erken sinirlanir.
 *  3. Greedy siralamalarla hizli bir cakismasiz kelime kumesi kurulur.
 *  4. Kisa sureli, sinirli backtracking bu sonucu iyilestirmeye calisir.
 *
 * Sonuc tam maksimum olmak zorunda degildir; ancak sayilan kelimeler her zaman
 * ortak hucre kullanmayan bir secime dayanir.
 */
class AvailableWordCounter {

    fun analyze(
        board: List<List<Cell>>,
        dictionary: WordDictionary,
        options: ComputeOptions = ComputeOptions()
    ): WordCountResult {
        val rows = board.size
        if (rows == 0) return WordCountResult.EMPTY
        val cols = board[0].size
        if (cols == 0) return WordCountResult.EMPTY

        val grid = CharArray(rows * cols)
        for (r in 0 until rows) {
            val row = board[r]
            require(row.size == cols) {
                "Tahta satirlarinin sutun sayilari esit olmali (dikdortgen grid)."
            }
            for (c in 0 until cols) {
                grid[r * cols + c] = TurkishTextNormalizer
                    .normalize(row[c].letter.toString())
                    .firstOrNull() ?: row[c].letter
            }
        }

        val visited = BooleanArray(rows * cols)
        val path = StringBuilder(options.maxLen.coerceAtLeast(options.minLen))
        val placementsByWord = LinkedHashMap<String, MutableList<CellMask>>()

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                dfs(
                    r = r,
                    c = c,
                    rows = rows,
                    cols = cols,
                    grid = grid,
                    visited = visited,
                    path = path,
                    placementsByWord = placementsByWord,
                    dictionary = dictionary,
                    options = options,
                    usedMask = CellMask.EMPTY
                )
            }
        }

        val candidates = buildCandidates(
            placementsByWord = placementsByWord,
            totalCells = rows * cols,
            options = options
        )

        return WordCountResult(
            count = computeFastIndependentWordCount(
                candidates = candidates,
                totalCells = rows * cols,
                minWordLength = options.minLen,
                options = options
            ),
            words = placementsByWord.keys
        )
    }

    fun count(
        board: List<List<Cell>>,
        dictionary: WordDictionary,
        options: ComputeOptions = ComputeOptions()
    ): Int = analyze(board, dictionary, options).count

    private fun dfs(
        r: Int,
        c: Int,
        rows: Int,
        cols: Int,
        grid: CharArray,
        visited: BooleanArray,
        path: StringBuilder,
        placementsByWord: MutableMap<String, MutableList<CellMask>>,
        dictionary: WordDictionary,
        options: ComputeOptions,
        usedMask: CellMask
    ) {
        val idx = r * cols + c
        if (visited[idx]) return
        if (path.length >= options.maxLen) return

        path.append(grid[idx])
        visited[idx] = true
        val nextMask = usedMask.withCell(idx)

        val prefix = path.toString()
        if (dictionary.hasPrefix(prefix)) {
            if (path.length >= options.minLen && dictionary.contains(prefix)) {
                recordPlacement(
                    word = prefix,
                    mask = nextMask,
                    rows = rows,
                    cols = cols,
                    placementsByWord = placementsByWord,
                    options = options
                )
            }

            for (offset in NEIGHBOR_OFFSETS) {
                val nr = r + offset.first
                val nc = c + offset.second
                if (nr in 0 until rows && nc in 0 until cols) {
                    dfs(
                        r = nr,
                        c = nc,
                        rows = rows,
                        cols = cols,
                        grid = grid,
                        visited = visited,
                        path = path,
                        placementsByWord = placementsByWord,
                        dictionary = dictionary,
                        options = options,
                        usedMask = nextMask
                    )
                }
            }
        }

        path.setLength(path.length - 1)
        visited[idx] = false
    }

    private fun recordPlacement(
        word: String,
        mask: CellMask,
        rows: Int,
        cols: Int,
        placementsByWord: MutableMap<String, MutableList<CellMask>>,
        options: ComputeOptions
    ) {
        val placements = placementsByWord.getOrPut(word) { ArrayList() }
        if (placements.any { it == mask }) return

        if (placements.size < options.rawPlacementsPerWord) {
            placements.add(mask)
            return
        }

        val newScore = staticPathCost(mask, rows, cols)
        var worstIndex = -1
        var worstScore = Int.MIN_VALUE
        for (index in placements.indices) {
            val score = staticPathCost(placements[index], rows, cols)
            if (score > worstScore) {
                worstScore = score
                worstIndex = index
            }
        }
        if (worstIndex >= 0 && newScore < worstScore) {
            placements[worstIndex] = mask
        }
    }

    private fun buildCandidates(
        placementsByWord: Map<String, List<CellMask>>,
        totalCells: Int,
        options: ComputeOptions
    ): List<WordCandidate> {
        val pressure = IntArray(totalCells)
        for (placements in placementsByWord.values) {
            for (placement in placements) {
                placement.forEachCell { pressure[it]++ }
            }
        }

        return placementsByWord.mapNotNull { (word, placements) ->
            val rankedPlacements = placements
                .sortedWith(
                    compareBy<CellMask> { placementPressure(it, pressure) }
                        .thenBy { it.cellCount() }
                )
                .take(options.keptPlacementsPerWord)

            if (rankedPlacements.isEmpty()) return@mapNotNull null

            WordCandidate(
                word = word,
                placements = rankedPlacements,
                bestPressure = rankedPlacements.minOf { placementPressure(it, pressure) }
            )
        }
    }

    private fun computeFastIndependentWordCount(
        candidates: List<WordCandidate>,
        totalCells: Int,
        minWordLength: Int,
        options: ComputeOptions
    ): Int {
        if (candidates.isEmpty()) return 0

        val greedyOrders = listOf(
            candidates.sortedWith(
                compareBy<WordCandidate> { it.word.length }
                    .thenBy { it.bestPressure }
                    .thenBy { it.placements.size }
            ),
            candidates.sortedWith(
                compareBy<WordCandidate> { it.placements.size }
                    .thenBy { it.word.length }
                    .thenBy { it.bestPressure }
            ),
            candidates.sortedWith(
                compareBy<WordCandidate> { it.bestPressure }
                    .thenBy { it.word.length }
                    .thenBy { it.placements.size }
            )
        )

        var best = 0
        for (order in greedyOrders) {
            best = maxOf(best, greedySelect(order, CellMask.EMPTY).count)
        }

        if (options.searchTimeoutMs <= 0L || options.backtrackingDepth <= 0) {
            return best
        }

        val deadlineNanos = System.nanoTime() + options.searchTimeoutMs * NANOS_PER_MS
        val searchOrder = candidates
            .sortedWith(
                compareBy<WordCandidate> { it.placements.size }
                    .thenBy { it.word.length }
                    .thenBy { it.bestPressure }
            )
            .take(options.backtrackingCandidateLimit)

        fun search(
            remaining: List<WordCandidate>,
            used: CellMask,
            chosen: Int,
            depthLeft: Int
        ) {
            if (System.nanoTime() >= deadlineNanos) return
            if (chosen > best) best = chosen

            val freeCells = totalCells - used.cellCount()
            if (chosen + (freeCells / minWordLength) <= best) return

            val greedy = greedySelect(remaining, used)
            best = maxOf(best, chosen + greedy.count)
            if (depthLeft <= 0 || remaining.isEmpty()) return

            val pivotInfo = choosePivot(
                candidates = remaining,
                used = used,
                windowSize = options.backtrackingWindowSize
            ) ?: return

            val pivot = pivotInfo.candidate
            val nextRemaining = remaining.filterNot { it.word == pivot.word }
            val branchPlacements = pivotInfo.feasiblePlacements
                .sortedWith(
                    compareBy<CellMask> { it.cellCount() }
                        .thenBy { placementCompatibilityLoss(it, nextRemaining) }
                )
                .take(options.branchPlacementLimit)

            for (placement in branchPlacements) {
                search(
                    remaining = nextRemaining,
                    used = used.union(placement),
                    chosen = chosen + 1,
                    depthLeft = depthLeft - 1
                )
            }

            search(
                remaining = nextRemaining,
                used = used,
                chosen = chosen,
                depthLeft = depthLeft - 1
            )
        }

        search(
            remaining = searchOrder,
            used = CellMask.EMPTY,
            chosen = 0,
            depthLeft = options.backtrackingDepth
        )

        return best
    }

    private fun greedySelect(
        candidates: List<WordCandidate>,
        initialUsed: CellMask
    ): GreedyResult {
        var used = initialUsed
        var count = 0

        for (candidate in candidates) {
            val placement = candidate.placements
                .asSequence()
                .filterNot { it.overlaps(used) }
                .minWithOrNull(
                    compareBy<CellMask> { it.cellCount() }
                        .thenBy { placementCompatibilityLoss(it, candidates) }
                ) ?: continue

            used = used.union(placement)
            count++
        }

        return GreedyResult(count = count, used = used)
    }

    private fun choosePivot(
        candidates: List<WordCandidate>,
        used: CellMask,
        windowSize: Int
    ): PivotInfo? {
        var bestCandidate: WordCandidate? = null
        var bestPlacements: List<CellMask> = emptyList()

        val limit = minOf(windowSize, candidates.size)
        for (index in 0 until limit) {
            val candidate = candidates[index]
            val feasible = candidate.placements.filterNot { it.overlaps(used) }
            if (feasible.isEmpty()) continue

            if (
                bestCandidate == null ||
                feasible.size < bestPlacements.size ||
                (feasible.size == bestPlacements.size && candidate.bestPressure < bestCandidate.bestPressure)
            ) {
                bestCandidate = candidate
                bestPlacements = feasible
            }
        }

        val candidate = bestCandidate ?: return null
        return PivotInfo(candidate, bestPlacements)
    }

    private fun placementCompatibilityLoss(
        placement: CellMask,
        candidates: List<WordCandidate>
    ): Int {
        var loss = 0
        for (candidate in candidates) {
            if (candidate.placements.all { it.overlaps(placement) }) {
                loss++
            }
        }
        return loss
    }

    private fun placementPressure(mask: CellMask, pressure: IntArray): Int {
        var score = 0
        mask.forEachCell { score += pressure[it] }
        return score
    }

    private fun staticPathCost(mask: CellMask, rows: Int, cols: Int): Int {
        var score = 0
        mask.forEachCell { index ->
            val row = index / cols
            val col = index % cols
            score += minOf(row, col, rows - 1 - row, cols - 1 - col)
        }
        return score
    }

    companion object {
        const val MIN_WORD_LENGTH: Int = 3
        const val DEFAULT_MAX_WORD_LENGTH: Int = 20

        private const val NANOS_PER_MS: Long = 1_000_000L

        private val NEIGHBOR_OFFSETS: Array<Pair<Int, Int>> = arrayOf(
            -1 to -1, -1 to 0, -1 to 1,
            0 to -1, 0 to 1,
            1 to -1, 1 to 0, 1 to 1
        )
    }

    data class ComputeOptions(
        val minLen: Int = MIN_WORD_LENGTH,
        val maxLen: Int = DEFAULT_MAX_WORD_LENGTH,
        val rawPlacementsPerWord: Int = 24,
        val keptPlacementsPerWord: Int = 6,
        val backtrackingCandidateLimit: Int = 120,
        val backtrackingWindowSize: Int = 28,
        val branchPlacementLimit: Int = 3,
        val backtrackingDepth: Int = 5,
        val searchTimeoutMs: Long = 70L
    ) {
        init {
            require(minLen >= 1) { "minLen >= 1 olmali." }
            require(maxLen >= minLen) { "maxLen >= minLen olmali." }
            require(rawPlacementsPerWord >= 1) { "rawPlacementsPerWord >= 1 olmali." }
            require(keptPlacementsPerWord >= 1) { "keptPlacementsPerWord >= 1 olmali." }
            require(backtrackingCandidateLimit >= 1) { "backtrackingCandidateLimit >= 1 olmali." }
            require(backtrackingWindowSize >= 1) { "backtrackingWindowSize >= 1 olmali." }
            require(branchPlacementLimit >= 1) { "branchPlacementLimit >= 1 olmali." }
            require(backtrackingDepth >= 0) { "backtrackingDepth >= 0 olmali." }
        }
    }

    private data class WordCandidate(
        val word: String,
        val placements: List<CellMask>,
        val bestPressure: Int
    )

    private data class GreedyResult(
        val count: Int,
        val used: CellMask
    )

    private data class PivotInfo(
        val candidate: WordCandidate,
        val feasiblePlacements: List<CellMask>
    )

    private data class CellMask(
        val lowBits: Long,
        val highBits: Long
    ) {
        fun withCell(index: Int): CellMask =
            if (index < 64) {
                copy(lowBits = lowBits or (1L shl index))
            } else {
                copy(highBits = highBits or (1L shl (index - 64)))
            }

        fun overlaps(other: CellMask): Boolean =
            (lowBits and other.lowBits) != 0L || (highBits and other.highBits) != 0L

        fun union(other: CellMask): CellMask =
            CellMask(
                lowBits = lowBits or other.lowBits,
                highBits = highBits or other.highBits
            )

        fun cellCount(): Int =
            java.lang.Long.bitCount(lowBits) + java.lang.Long.bitCount(highBits)

        fun forEachCell(action: (Int) -> Unit) {
            var low = lowBits
            while (low != 0L) {
                val bit = java.lang.Long.numberOfTrailingZeros(low)
                action(bit)
                low = low and (low - 1)
            }

            var high = highBits
            while (high != 0L) {
                val bit = java.lang.Long.numberOfTrailingZeros(high)
                action(bit + 64)
                high = high and (high - 1)
            }
        }

        companion object {
            val EMPTY: CellMask = CellMask(0L, 0L)
        }
    }
}

/**
 * @property count Ortak hucre kullanmadan hizli yaklasik cozumle secilen
 *                 kelime sayisi.
 * @property words Tahtada tespit edilen benzersiz gecerli kelimeler.
 */
data class WordCountResult(
    val count: Int,
    val words: Set<String>
) {
    companion object {
        val EMPTY: WordCountResult = WordCountResult(0, emptySet())
    }
}
