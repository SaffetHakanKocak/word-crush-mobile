package com.saffet.wordcrushmobile.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.saffet.wordcrushmobile.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * `user_profile` tablosu için DAO.
 *
 * Tek-satırlı bir tablo (id = 1) olduğu için DAO yüzeyi oldukça yalındır.
 * Sabit [UserProfileEntity.SINGLE_USER_ID] değeri SQL sabit olarak gömülür;
 * KSP 2 + Room 2.6.x kombinasyonunda default parameter'lı suspend query'lerin
 * tetiklediği "unexpected jvm signature V" hatasından kaçınmak için
 * hiçbir suspend fonksiyon default değer veya nullable primitive dönüşü
 * içermez.
 */
@Dao
interface UserProfileDao {

    // Dönüş tipi `Long` — eklenen satırın rowId'si. `Unit` (V) dönüşü
    // KSP 2.0.x + Room 2.6.x'te "unexpected jvm signature V" hatasına yol
    // açıyor; `Long` (J) bu sorunu bertaraf eder.
    @Upsert
    suspend fun upsert(entity: UserProfileEntity): Long

    /** Tek satırlık profili gözlemler. Yoksa akışta `null` döner. */
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun observe(): Flow<UserProfileEntity?>

    /** Profili bir kez okur; yoksa `null`. */
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun get(): UserProfileEntity?

    /**
     * Altını [delta] kadar değiştirir (negatif olabilir).
     * 0'ın altına düşmeye izin verilmez (`MAX(..., 0)`).
     *
     * @return Etkilenen satır sayısı. Çağrı noktalarının bu sonucu
     *         kullanmasına gerek yoktur; Room 2.6 + KSP 2 uyumu için
     *         Unit yerine Int döndürüyoruz.
     */
    @Query("UPDATE user_profile SET gold = MAX(gold + :delta, 0) WHERE id = 1")
    suspend fun adjustGold(delta: Int): Int
}
