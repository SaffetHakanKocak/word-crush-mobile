package com.saffet.wordcrushmobile.data.repository

import com.saffet.wordcrushmobile.data.local.dao.GameDao
import com.saffet.wordcrushmobile.data.local.entity.GameEntity

/**
 * Veri kaynakları (DAO, remote, preferences) ile domain katmanı arasında köprü görevi görür.
 * UI ve ViewModel'ler verilere yalnızca bu sınıf üzerinden erişmelidir.
 */
class GameRepository(
    private val gameDao: GameDao? = null
) {
    suspend fun saveGame(game: GameEntity): Long {
        return gameDao?.insert(game) ?: 0L
    }

    suspend fun getAllGames(): List<GameEntity> {
        return gameDao?.getAll() ?: emptyList()
    }
}
