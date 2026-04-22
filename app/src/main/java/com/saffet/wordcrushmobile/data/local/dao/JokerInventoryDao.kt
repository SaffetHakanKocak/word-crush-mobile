package com.saffet.wordcrushmobile.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.saffet.wordcrushmobile.data.local.entity.JokerInventoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * `joker_inventory` tablosu için DAO.
 *
 * Ana işlem `@Upsert` — aynı joker için satır varsa güncelle, yoksa ekle.
 * Bu sayede Market'te satın alma / oyunda kullanma akışları basit kalır.
 */
@Dao
interface JokerInventoryDao {

    // Dönüş tipi `Long` — eklenen satırın rowId'si. KSP 2.0.x + Room 2.6.x'te
    // `Unit` (V) dönüş tipi "unexpected jvm signature V" hatasına yol açıyor;
    // `Long` (J) kullanmak bu JVM descriptor sorununu tamamen bertaraf eder.
    @Upsert
    suspend fun upsert(entity: JokerInventoryEntity): Long

    @Query("SELECT * FROM joker_inventory ORDER BY joker_type ASC")
    fun observeAll(): Flow<List<JokerInventoryEntity>>

    @Query("SELECT * FROM joker_inventory WHERE joker_type = :type LIMIT 1")
    suspend fun getByType(type: String): JokerInventoryEntity?
}
