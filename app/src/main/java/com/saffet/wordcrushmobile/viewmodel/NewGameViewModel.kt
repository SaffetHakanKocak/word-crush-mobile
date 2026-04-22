package com.saffet.wordcrushmobile.viewmodel

import androidx.lifecycle.ViewModel
import com.saffet.wordcrushmobile.domain.model.GameDifficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Yeni oyun ekranının state'ini yönetir.
 *
 * Basit bir seçim ekranı olduğu için DataStore veya Application-context
 * bağımlılığı yoktur; saf [ViewModel] yeterlidir.
 *
 * [selectedDifficulty] kullanıcı başka bir zorluk seçtiğinde anında
 * reaktif olarak güncellenir ve hamle/boyut özeti bu değere göre gösterilir.
 */
class NewGameViewModel : ViewModel() {

    private val _selectedDifficulty = MutableStateFlow(GameDifficulty.DEFAULT)
    val selectedDifficulty: StateFlow<GameDifficulty> = _selectedDifficulty.asStateFlow()

    fun select(difficulty: GameDifficulty) {
        _selectedDifficulty.value = difficulty
    }
}
