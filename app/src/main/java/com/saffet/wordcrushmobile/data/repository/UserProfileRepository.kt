package com.saffet.wordcrushmobile.data.repository

import com.saffet.wordcrushmobile.data.local.dao.UserProfileDao
import com.saffet.wordcrushmobile.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Tek-kullanıcı profili için repository. Şu an yalnızca altın miktarını
 * ve ilk oluşum zamanını yönetir.
 *
 * [ensureInitialized] uygulama ilk açıldığında bir kez çağrılmalıdır
 * (bkz. `WordCrushApp.onCreate`). Bu çağrı yoksa altın değerleri 0'dır.
 */
class UserProfileRepository(
    private val dao: UserProfileDao
) {

    /** Güncel altın miktarı akışı (profil yoksa 0). */
    fun observeGold(): Flow<Int> =
        dao.observe().map { it?.gold ?: 0 }

    suspend fun getGold(): Int = dao.get()?.gold ?: 0

    /**
     * Profil daha önce oluşturulmamışsa başlangıç altınıyla oluşturur.
     * İdempotent — birden çok kez çağrılsa bile mevcut veriyi ezmez.
     */
    suspend fun ensureInitialized() {
        if (dao.get() == null) {
            dao.upsert(
                UserProfileEntity(
                    id = UserProfileEntity.SINGLE_USER_ID,
                    gold = UserProfileEntity.INITIAL_GOLD,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    /** Altını delta kadar değiştirir (negatif = harcama). */
    suspend fun adjustGold(delta: Int): Int {
        ensureInitialized()
        dao.adjustGold(delta)
        return getGold()
    }
}
