package com.saffet.wordcrushmobile.domain.usecase

import com.saffet.wordcrushmobile.domain.dictionary.WordDictionary
import com.saffet.wordcrushmobile.domain.engine.AvailableWordCounter
import com.saffet.wordcrushmobile.domain.engine.WordCrushEngine
import com.saffet.wordcrushmobile.domain.model.Cell
import com.saffet.wordcrushmobile.domain.model.GameConfig

/**
 * Tahtada en az bir geçerli kelime bulunduğunu garanti etmeye çalışan use case.
 *
 * Strateji sırası:
 *  1. Mevcut tahtayı analiz et. (count > 0 ise hiçbir şey yapma)
 *  2. Kontrollü reshuffle dene (harf/special çokluğunu korur).
 *  3. Olmazsa yeniden üret (Türkçe frekans havuzu korunur).
 *  4. Son çare olarak küçük bir "garanti kelime" enjekte et.
 *
 * Not: 4. adım, sözlükte bulunan bir kelimeyi tahtaya yerleştirerek
 * kullanıcıya çözülemez grid gösterilmemesini sağlar.
 */
class EnsurePlayableBoardUseCase(
    private val engine: WordCrushEngine,
    private val counter: AvailableWordCounter = AvailableWordCounter(),
    private val maxReshuffleAttempts: Int = 3,
    private val maxRegenerateAttempts: Int = 12,
    private val fallbackGuaranteeWords: List<String> = listOf(
        "ANA", "ARA", "KAR", "KAN", "SU", "DEN", "ELI", "ALI"
    )
) {

    init {
        require(maxReshuffleAttempts >= 0) { "maxReshuffleAttempts >= 0 olmalı" }
        require(maxRegenerateAttempts >= 0) { "maxRegenerateAttempts >= 0 olmalı" }
    }

    fun ensure(
        board: List<List<Cell>>,
        config: GameConfig,
        dictionary: WordDictionary
    ): RecoveryResult {
        if (board.isEmpty()) {
            val regenerated = runRegenerateAttempts(config, dictionary)
            if (regenerated != null) return regenerated
            return RecoveryResult(
                board = board,
                availableWordCount = 0,
                intervention = Intervention.NONE
            )
        }

        val initialCount = counter.count(board, dictionary)
        if (initialCount > 0) {
            return RecoveryResult(
                board = board,
                availableWordCount = initialCount,
                intervention = Intervention.NONE
            )
        }

        var reshuffled = board
        repeat(maxReshuffleAttempts) {
            reshuffled = engine.reshuffleBoard(reshuffled)
            val count = counter.count(reshuffled, dictionary)
            if (count > 0) {
                return RecoveryResult(
                    board = reshuffled,
                    availableWordCount = count,
                    intervention = Intervention.RESHUFFLED
                )
            }
        }

        val regenerated = runRegenerateAttempts(config, dictionary)
        if (regenerated != null) return regenerated

        val injected = injectGuaranteedWord(reshuffled, dictionary)
        if (injected != null) {
            val count = counter.count(injected, dictionary)
            if (count > 0) {
                return RecoveryResult(
                    board = injected,
                    availableWordCount = count,
                    intervention = Intervention.GUARANTEED_WORD_INJECTED
                )
            }
        }

        return RecoveryResult(
            board = reshuffled,
            availableWordCount = 0,
            intervention = Intervention.FAILED
        )
    }

    private fun runRegenerateAttempts(
        config: GameConfig,
        dictionary: WordDictionary
    ): RecoveryResult? {
        var candidate = emptyList<List<Cell>>()
        repeat(maxRegenerateAttempts) {
            candidate = engine.generateBoard(config)
            val count = counter.count(candidate, dictionary)
            if (count > 0) {
                return RecoveryResult(
                    board = candidate,
                    availableWordCount = count,
                    intervention = Intervention.REGENERATED
                )
            }
        }
        return null
    }

    private fun injectGuaranteedWord(
        board: List<List<Cell>>,
        dictionary: WordDictionary
    ): List<List<Cell>>? {
        if (board.isEmpty()) return null
        val rows = board.size
        val cols = board.first().size
        if (rows == 0 || cols == 0) return null

        val word = fallbackGuaranteeWords
            .firstOrNull { candidate ->
                candidate.length >= 3 &&
                    (candidate.length <= cols || candidate.length <= rows) &&
                    dictionary.contains(candidate.lowercase())
            }
            ?: return null

        val normalized = word.uppercase()
        return if (normalized.length <= cols) {
            // Yatay bir yol yerleştir: (0,0) -> (0,1) -> ...
            board.mapIndexed { r, row ->
                row.mapIndexed { c, cell ->
                    if (r == 0 && c < normalized.length) {
                        cell.copy(letter = normalized[c], isSelected = false)
                    } else {
                        cell.copy(isSelected = false)
                    }
                }
            }
        } else if (normalized.length <= rows) {
            // Dikey bir yol yerleştir: (0,0) -> (1,0) -> ...
            board.mapIndexed { r, row ->
                row.mapIndexed { c, cell ->
                    if (c == 0 && r < normalized.length) {
                        cell.copy(letter = normalized[r], isSelected = false)
                    } else {
                        cell.copy(isSelected = false)
                    }
                }
            }
        } else {
            null
        }
    }
}

enum class Intervention {
    NONE,
    RESHUFFLED,
    REGENERATED,
    GUARANTEED_WORD_INJECTED,
    FAILED
}

data class RecoveryResult(
    val board: List<List<Cell>>,
    val availableWordCount: Int,
    val intervention: Intervention
)
