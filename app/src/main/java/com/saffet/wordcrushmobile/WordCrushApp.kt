package com.saffet.wordcrushmobile

import android.app.Application
import com.saffet.wordcrushmobile.data.dictionary.AssetDictionarySource
import com.saffet.wordcrushmobile.data.local.db.WordCrushDatabase
import com.saffet.wordcrushmobile.data.preferences.UserPreferencesRepository
import com.saffet.wordcrushmobile.data.repository.DefaultDictionaryRepository
import com.saffet.wordcrushmobile.data.repository.GameHistoryRepository
import com.saffet.wordcrushmobile.data.repository.JokerInventoryRepository
import com.saffet.wordcrushmobile.data.repository.UserProfileRepository
import com.saffet.wordcrushmobile.domain.dictionary.DictionaryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Uygulamanın Application sınıfı.
 *
 * DataStore, Room ve Dictionary gibi application-scoped (tekil) bileşenlerin
 * burada lazy olarak başlatılması önerilir. Böylece AndroidViewModel içinden
 * Application cast edilerek aynı repository örneğine erişilebilir.
 *
 * Hilt gibi bir DI kütüphanesi eklendiğinde bu sınıf @HiltAndroidApp ile
 * işaretlenecek ve lazy initializers yerine @Inject kullanılabilecektir.
 */
class WordCrushApp : Application() {

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(applicationContext)
    }

    val dictionaryRepository: DictionaryRepository by lazy {
        DefaultDictionaryRepository(
            source = AssetDictionarySource(applicationContext)
        )
    }

    // --- Room ----------------------------------------------------------

    private val database: WordCrushDatabase by lazy {
        WordCrushDatabase.getInstance(applicationContext)
    }

    val gameHistoryRepository: GameHistoryRepository by lazy {
        GameHistoryRepository(database.gameHistoryDao())
    }

    val jokerInventoryRepository: JokerInventoryRepository by lazy {
        JokerInventoryRepository(database.jokerInventoryDao())
    }

    val userProfileRepository: UserProfileRepository by lazy {
        UserProfileRepository(database.userProfileDao())
    }

    /**
     * Application ömrü boyunca yaşayan coroutine scope.
     * Sözlük önyüklemesi gibi "fire-and-forget" arkaplan işlerinde kullanılır.
     */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            // Sözlüğü arka planda yükle; UI akışını bloklamasın.
            // Kullanıcı oyuna başlayana kadar zaten hazır olacaktır.
            dictionaryRepository.preload()
        }

        applicationScope.launch {
            // Profil satırını garanti altına al — Market ilk açıldığında
            // altın miktarı 0 yerine INITIAL_GOLD olarak görünür.
            userProfileRepository.ensureInitialized()
        }
    }
}
