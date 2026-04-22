package com.saffet.wordcrushmobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.saffet.wordcrushmobile.WordCrushApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Kullanıcı adı giriş/değiştirme ekranının ViewModel'i.
 *
 * - [input] kullanıcının text field'daki anlık girdisi.
 * - [isValid] girdinin kayıt için uygun olup olmadığı (boşluklar temizlendiğinde
 *   uzunluk minimum koşulunu sağlıyor mu).
 * - [save] değeri DataStore'a persist eder ve başarılı olunca [onSaved] callback'ini
 *   main thread'de tetikler.
 */
class UsernameViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as WordCrushApp).userPreferencesRepository

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()

    val isValid: Boolean
        get() = _input.value.trim().length >= MIN_USERNAME_LENGTH

    fun onInputChange(value: String) {
        _input.value = value
    }

    fun save(onSaved: () -> Unit) {
        val trimmed = _input.value.trim()
        if (trimmed.length < MIN_USERNAME_LENGTH) return
        viewModelScope.launch {
            repo.saveUsername(trimmed)
            onSaved()
        }
    }

    companion object {
        const val MIN_USERNAME_LENGTH = 2
    }
}
