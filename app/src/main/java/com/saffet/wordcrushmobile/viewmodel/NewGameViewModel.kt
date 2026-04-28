package com.saffet.wordcrushmobile.viewmodel

import androidx.lifecycle.ViewModel
import com.saffet.wordcrushmobile.domain.model.GridSize
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

    private val _selectedGridSize = MutableStateFlow(GridSize.DEFAULT)
    val selectedGridSize: StateFlow<GridSize> = _selectedGridSize.asStateFlow()

    fun select(gridSize: GridSize) {
        _selectedGridSize.value = gridSize
    }
}
