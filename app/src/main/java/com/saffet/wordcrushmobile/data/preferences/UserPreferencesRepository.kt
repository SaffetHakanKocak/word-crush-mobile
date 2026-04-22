package com.saffet.wordcrushmobile.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore dosyasının application-context üzerinden tekil erişimi.
 *
 * Dosya adı bir kez verildiği için aynı prosesin yalnızca bir DataStore
 * örneğine sahip olması garanti altına alınır.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = UserPreferencesRepository.DATASTORE_NAME
)

/**
 * Kullanıcı tercihlerini (şimdilik yalnızca kullanıcı adı) okuyup yazan repository.
 *
 * - [usernameFlow] DataStore'daki değeri reaktif olarak yayar; null ise kullanıcı
 *   henüz kayıtlı değildir.
 * - [saveUsername] boşlukları temizleyip değeri persist eder.
 * - [clearUsername] değeri tamamen siler (test veya "hesabı sıfırla" senaryoları için).
 */
class UserPreferencesRepository(private val context: Context) {

    val usernameFlow: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[KEY_USERNAME]?.takeIf { it.isNotBlank() } }

    suspend fun saveUsername(username: String) {
        val trimmed = username.trim()
        context.dataStore.edit { prefs ->
            prefs[KEY_USERNAME] = trimmed
        }
    }

    suspend fun clearUsername() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_USERNAME)
        }
    }

    companion object {
        const val DATASTORE_NAME = "user_prefs"
        private val KEY_USERNAME = stringPreferencesKey("username")
    }
}
