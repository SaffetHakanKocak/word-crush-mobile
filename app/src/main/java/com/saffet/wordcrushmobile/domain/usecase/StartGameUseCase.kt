package com.saffet.wordcrushmobile.domain.usecase

import com.saffet.wordcrushmobile.domain.engine.WordCrushEngine
import com.saffet.wordcrushmobile.domain.model.GameModel

/**
 * Yeni bir oyun başlatmak için kullanılan use case (geçici).
 *
 * Not: İleride yeni [com.saffet.wordcrushmobile.domain.model.GameState]
 * modeline geçilecektir; bu sınıf eski [GameModel]'i kullanmaya devam eder
 * çünkü hâlâ [com.saffet.wordcrushmobile.viewmodel.GameViewModel] placeholder
 * tarafından referanslanıyor.
 */
class StartGameUseCase(
    private val engine: WordCrushEngine = WordCrushEngine()
) {
    operator fun invoke(rows: Int = 6, cols: Int = 6): GameModel {
        val board = engine.generateBoard(rows, cols)
            .map { row -> row.map { cell -> cell.letter } }
        return GameModel(board = board)
    }
}
