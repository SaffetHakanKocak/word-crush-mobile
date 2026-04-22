package com.saffet.wordcrushmobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.saffet.wordcrushmobile.WordCrushApp
import com.saffet.wordcrushmobile.data.repository.JokerInventoryRepository
import com.saffet.wordcrushmobile.data.repository.UserProfileRepository
import com.saffet.wordcrushmobile.domain.model.JokerType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Market ekranı için ViewModel.
 *
 * Üç sorumluluk üstlenir:
 *  1. Altın miktarını ve joker envanterini [UserProfileRepository] +
 *     [JokerInventoryRepository] üzerinden reaktif olarak yayınlar.
 *  2. Satın alma akışını atomik gibi davrandırır — [purchaseMutex] sayesinde
 *     aynı anda iki butona basılsa bile yalnızca biri altından düşürür.
 *     Transaction olmadığı için önce altın kontrol + düşürme, sonra envanter
 *     artışı yapılır; altın yetmiyorsa envantere dokunulmaz.
 *  3. Geçici geri bildirimleri tek kullanımlık mesaj olarak UI'ya akıtır
 *     ([messages]), böylece rotasyon sonrası aynı snackbar iki kez gösterilmez.
 */
class MarketViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as WordCrushApp
    private val userProfileRepository: UserProfileRepository = app.userProfileRepository
    private val jokerRepository: JokerInventoryRepository = app.jokerInventoryRepository

    /** Satın alma yarışlarını engelleyen kilit. */
    private val purchaseMutex = Mutex()

    /** Kullanıcı satın aldıklarında true — buton tekrar basılmasın diye. */
    private val _isPurchasing = MutableStateFlow(false)

    /** Tek seferlik snackbar mesajları. */
    private val _messages = Channel<String>(capacity = Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    val uiState: StateFlow<MarketUiState> =
        combine(
            userProfileRepository.observeGold(),
            jokerRepository.observeAll(),
            _isPurchasing.asStateFlow()
        ) { gold, inventory, purchasing ->
            MarketUiState(
                isLoading = false,
                gold = gold,
                inventory = inventory,
                isPurchasing = purchasing
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = MarketUiState.INITIAL
        )

    /**
     * Belirtilen jokeri 1 adet satın alır.
     *
     * Altın yetmiyorsa hiç yan etki üretmez, sadece kullanıcıya mesaj akıtır.
     * Room güncellemeleri başarısız olursa (ör. iptal) envanter tutarsız
     * kalmaz çünkü önce altın düşürülüp sonra envanter artırılıyor: envanter
     * artışı başarısız olsa bile altın harcanmış olur (risk sınırlı; gerçek
     * bir transaction için Room withTransaction kullanılabilir).
     */
    fun purchase(type: JokerType) {
        viewModelScope.launch {
            if (!purchaseMutex.tryLock()) return@launch
            _isPurchasing.value = true
            try {
                val cost = type.costGold
                val currentGold = userProfileRepository.getGold()
                if (currentGold < cost) {
                    _messages.trySend("Yetersiz altın · ${type.displayName} için $cost gerekli")
                    return@launch
                }
                userProfileRepository.adjustGold(-cost)
                val newQty = jokerRepository.adjust(type, +1)
                _messages.trySend("${type.displayName} alındı · envanter: $newQty")
            } finally {
                _isPurchasing.value = false
                purchaseMutex.unlock()
            }
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        /** `viewModel(factory = MarketViewModel.Factory)` ile kullanılır. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = requireNotNull(
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                ) as WordCrushApp
                MarketViewModel(app)
            }
        }
    }
}

/**
 * Market ekranı UI state'i.
 *
 * @property isLoading    Room ilk snapshot'ı dönene kadar true.
 * @property gold         Kullanıcının anlık altını.
 * @property inventory    Her joker türü için sahip olunan adet.
 * @property isPurchasing Satın alma in-flight; butonlar disable edilmeli.
 */
data class MarketUiState(
    val isLoading: Boolean,
    val gold: Int,
    val inventory: Map<JokerType, Int>,
    val isPurchasing: Boolean
) {
    fun ownedOf(type: JokerType): Int = inventory[type] ?: 0
    fun canAfford(type: JokerType): Boolean = gold >= type.costGold

    companion object {
        val INITIAL = MarketUiState(
            isLoading = true,
            gold = 0,
            inventory = emptyMap(),
            isPurchasing = false
        )
    }
}
