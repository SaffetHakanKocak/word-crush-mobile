package com.saffet.wordcrushmobile.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.saffet.wordcrushmobile.data.local.dao.GameHistoryDao
import com.saffet.wordcrushmobile.data.local.dao.JokerInventoryDao
import com.saffet.wordcrushmobile.data.local.dao.UserProfileDao
import com.saffet.wordcrushmobile.data.local.entity.GameHistoryEntity
import com.saffet.wordcrushmobile.data.local.entity.JokerInventoryEntity
import com.saffet.wordcrushmobile.data.local.entity.UserProfileEntity

/**
 * Uygulamanın Room veritabanı.
 *
 * - Tek instance — [getInstance] double-check locking ile thread-safe.
 * - Şema versiyonu 1. Üretimde migration yazılmalıdır; geliştirme
 *   sürecinde `fallbackToDestructiveMigration()` kullanıyoruz.
 * - Entity'ler tüm primitive/String alanlar olduğu için TypeConverter'a
 *   gerek yoktur (tarihler Long epoch-ms, enumlar String).
 */
@Database(
    entities = [
        GameHistoryEntity::class,
        JokerInventoryEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WordCrushDatabase : RoomDatabase() {

    abstract fun gameHistoryDao(): GameHistoryDao
    abstract fun jokerInventoryDao(): JokerInventoryDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        private const val DB_NAME = "wordcrush.db"

        @Volatile
        private var INSTANCE: WordCrushDatabase? = null

        fun getInstance(context: Context): WordCrushDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room
                    .databaseBuilder(
                        context.applicationContext,
                        WordCrushDatabase::class.java,
                        DB_NAME
                    )
                    // Geliştirme: şema değişirse veriyi silip baştan kur.
                    // Üretimde yerine gerçek Migration'lar yazılmalıdır.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
