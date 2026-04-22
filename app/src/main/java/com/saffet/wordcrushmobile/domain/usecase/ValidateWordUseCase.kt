package com.saffet.wordcrushmobile.domain.usecase

import com.saffet.wordcrushmobile.domain.dictionary.DictionaryRepository
import com.saffet.wordcrushmobile.domain.engine.WordCrushEngine

/**
 * Bir kelimenin oyun için geçerli olup olmadığını belirleyen use case.
 *
 * Kontroller:
 *  1. Kelime, minimum harf sayısını sağlıyor mu
 *     (varsayılan: [WordCrushEngine.MIN_WORD_LENGTH]).
 *  2. Sözlükte mevcut mu ([DictionaryRepository.contains]).
 *
 * Motor, yapısal doğrulamayı (komşuluk / tekrar) çoktan yapar; burada ise
 * anlamsal doğrulama (gerçek bir Türkçe kelime mi?) yapılır. İki katman
 * birbirini tamamlar.
 */
class ValidateWordUseCase(
    private val dictionaryRepository: DictionaryRepository,
    private val minLength: Int = WordCrushEngine.MIN_WORD_LENGTH
) {
    suspend operator fun invoke(word: String): Boolean {
        if (word.length < minLength) return false
        return dictionaryRepository.contains(word)
    }
}
