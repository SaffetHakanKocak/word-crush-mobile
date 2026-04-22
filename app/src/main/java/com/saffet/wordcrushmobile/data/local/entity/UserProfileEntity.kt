package com.saffet.wordcrushmobile.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Uygulama tek-kullanıcı olduğu için profilin sabit bir satırı vardır
 * (`id = 1`). Şu an yalnızca "altın" (Market'te joker alımı için) ve
 * oluşturulma zamanı saklar. Username halen [com.saffet.wordcrushmobile
 * .data.preferences.UserPreferencesRepository] (DataStore) üzerinde tutulur;
 * böylece splash ekranı DB init'i beklemez.
 *
 * İleride çok-profil desteği gerekirse `id` auto-generate'e çekilir ve
 * sabit 1 kısıtı kaldırılır.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = SINGLE_USER_ID,

    /** Oyun içi altın miktarı. Başlangıçta [INITIAL_GOLD]. */
    val gold: Int,

    /** Profilin oluşturulduğu an (epoch millis). */
    @ColumnInfo(name = "created_at")
    val createdAt: Long
) {
    companion object {
        const val SINGLE_USER_ID: Int = 1

        /**
         * Şartname gereği kullanıcıya başlangıçta "yüksek miktarda" altın
         * verilir (para sistemi olmadığı için test deneyimini rahatlatmak
         * amacıyla).
         */
        const val INITIAL_GOLD: Int = 10_000
    }
}
