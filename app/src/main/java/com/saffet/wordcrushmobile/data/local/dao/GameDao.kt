package com.saffet.wordcrushmobile.data.local.dao

import com.saffet.wordcrushmobile.data.local.entity.GameEntity

/**
 * GameEntity kayıtlarına erişim için Data Access Object arayüzü.
 * Room kütüphanesi eklendiğinde @Dao anotasyonu ile işaretlenecektir.
 */
interface GameDao {
    suspend fun insert(game: GameEntity): Long
    suspend fun getAll(): List<GameEntity>
    suspend fun deleteAll()
}
