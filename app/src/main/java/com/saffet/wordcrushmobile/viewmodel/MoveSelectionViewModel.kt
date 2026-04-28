package com.saffet.wordcrushmobile.viewmodel

import androidx.lifecycle.ViewModel
import com.saffet.wordcrushmobile.domain.model.MoveOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Hamle sayısı seçim ekranının state'ini yönetir.
 */
class MoveSelectionViewModel : ViewModel() {

    private val _selectedMoveOption = MutableStateFlow(MoveOption.DEFAULT)
    val selectedMoveOption: StateFlow<MoveOption> = _selectedMoveOption.asStateFlow()

    fun select(moveOption: MoveOption) {
        _selectedMoveOption.value = moveOption
    }
}
