package com.saffet.wordcrushmobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.saffet.wordcrushmobile.WordCrushApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Ana menü ekranının ViewModel'i.
 *
 * [username] DataStore'daki kullanıcı adını reaktif olarak sunar. DataStore'dan
 * gelen Flow, [stateIn] ile UI için uygun bir StateFlow'a dönüştürülür:
 *
 * - [SharingStarted.WhileSubscribed] ile abone olan yokken upstream iptal edilir,
 *   böylece boşuna kaynak tüketilmez.
 * - Başlangıç değeri boş string; UI bu değerden gerçek değere smooth şekilde geçer.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as WordCrushApp).userPreferencesRepository

    val username: StateFlow<String> = repo.usernameFlow
        .map { it.orEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = ""
        )

    /**
     * Kullanıcı adını DataStore'a yazar.
     *
     * Değer normalize edilir (trim) ve minimum uzunluk kontrolünden geçer.
     * Geçerli değilse sessizce yok sayılır — UI zaten "Kaydet" butonunu
     * [isUsernameValid] üzerinden disable tutmalıdır.
     *
     * Mevcut [UsernameViewModel.save] ilk kayıt akışını (Splash →
     * Username ekranı) bozmadan aynı repository API'sini kullanır.
     */
    fun saveUsername(value: String) {
        if (!isUsernameValid(value)) return
        viewModelScope.launch {
            repo.saveUsername(value.trim())
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        /** UsernameViewModel ile aynı minimum uzunluk kuralı. */
        const val MIN_USERNAME_LENGTH = UsernameViewModel.MIN_USERNAME_LENGTH

        fun isUsernameValid(value: String): Boolean =
            value.trim().length >= MIN_USERNAME_LENGTH
    }
}
