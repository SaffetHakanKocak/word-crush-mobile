package com.saffet.wordcrushmobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.saffet.wordcrushmobile.WordCrushApp
import com.saffet.wordcrushmobile.domain.model.GameRecord
import com.saffet.wordcrushmobile.domain.model.GameStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Skor Tablosu ekranının state'ini sağlayan ViewModel.
 *
 * İki bağımsız Room akışını ([GameHistoryRepository.observeStats] ve
 * [GameHistoryRepository.observeAll]) tek bir [ScoreboardUiState] içinde
 * birleştirir. Veri Room'dan reaktif akar, ViewModel yalnızca dönüşüm ve
 * paylaşım (stateIn) sorumluluğunu üstlenir — UI'yi bloklayacak iş yapmaz.
 *
 * [SharingStarted.WhileSubscribed] sayesinde ekran kapalıyken upstream
 * aboneliği iptal edilir; kullanıcı tekrar açtığında son emit'i anında
 * aynı nesneden alır.
 */
class ScoreboardViewModel(application: Application) : AndroidViewModel(application) {

    private val historyRepository =
        (application as WordCrushApp).gameHistoryRepository

    val uiState: StateFlow<ScoreboardUiState> =
        combine(
            historyRepository.observeStats(),
            historyRepository.observeAll()
        ) { stats, records ->
            ScoreboardUiState(
                isLoading = false,
                stats = stats,
                records = records
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = ScoreboardUiState.INITIAL
        )

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        /** `viewModel(factory = ScoreboardViewModel.Factory)` ile kullanılır. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = requireNotNull(
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                ) as WordCrushApp
                ScoreboardViewModel(app)
            }
        }
    }
}

/**
 * Skor tablosu UI state'i.
 *
 * @property isLoading İlk emit beklenirken true; Room ilk snapshot'ı dönünce
 *                    false olur. Boş veri durumundan ayırt edilebilmesi için
 *                    ayrı bir bayraktır.
 * @property stats     Özet istatistikler; veri yoksa [GameStats.EMPTY].
 * @property records   En yeni oyun başta olacak şekilde tüm kayıtlar.
 */
data class ScoreboardUiState(
    val isLoading: Boolean,
    val stats: GameStats,
    val records: List<GameRecord>
) {
    /** Hiç oyun kaydı yok mu? Boş-state UI'si bu alana bakar. */
    val isEmpty: Boolean get() = !isLoading && records.isEmpty()

    companion object {
        val INITIAL = ScoreboardUiState(
            isLoading = true,
            stats = GameStats.EMPTY,
            records = emptyList()
        )
    }
}
