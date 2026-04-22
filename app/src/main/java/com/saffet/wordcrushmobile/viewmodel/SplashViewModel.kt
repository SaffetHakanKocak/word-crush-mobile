package com.saffet.wordcrushmobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.saffet.wordcrushmobile.WordCrushApp
import com.saffet.wordcrushmobile.ui.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Splash ekranının state'ini yönetir.
 *
 * Splash ekranı açılırken DataStore'dan kullanıcı adının kayıtlı olup olmadığı
 * kontrol edilir. Sonuca göre bir sonraki hedef route [nextRoute] olarak yayılır.
 *
 * - Kullanıcı adı kayıtlıysa → [Screen.Home]
 * - Kayıtlı değilse → [Screen.Username]
 */
class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as WordCrushApp).userPreferencesRepository

    private val _nextRoute = MutableStateFlow<String?>(null)
    val nextRoute: StateFlow<String?> = _nextRoute.asStateFlow()

    init {
        viewModelScope.launch {
            val stored = repo.usernameFlow.first()
            delay(SPLASH_DELAY_MS)
            _nextRoute.value = if (stored.isNullOrBlank()) {
                Screen.Username.route
            } else {
                Screen.Home.route
            }
        }
    }

    companion object {
        private const val SPLASH_DELAY_MS = 1200L
    }
}
