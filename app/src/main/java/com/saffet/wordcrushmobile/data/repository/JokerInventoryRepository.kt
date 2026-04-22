package com.saffet.wordcrushmobile.data.repository

import com.saffet.wordcrushmobile.data.local.dao.JokerInventoryDao
import com.saffet.wordcrushmobile.data.local.entity.JokerInventoryEntity
import com.saffet.wordcrushmobile.domain.model.JokerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Joker envanteri için repository. Market & Oyun ekranları bu sınıfı kullanır.
 *
 * Enum ↔ String dönüşümü repository içinde yapılır; DB dışarıdan "SHUFFLE"
 * gibi string anahtarlarla konuşur. Yeni bir joker tipi eklendiğinde yalnızca
 * [JokerType] enum'una eklemek yeterlidir; şema değişmez.
 */
class JokerInventoryRepository(
    private val dao: JokerInventoryDao
) {

    /**
     * Tüm joker tiplerinin güncel miktarları.
     * Envanterde satırı olmayan türler 0 kabul edilir; dönen map
     * [JokerType]'ın tüm elemanlarını garanti eder.
     */
    fun observeAll(): Flow<Map<JokerType, Int>> =
        dao.observeAll().map { list ->
            val byType = list.associate { parse(it.jokerType) to it.quantity }
                .filterKeys { it != null }
                .mapKeys { it.key!! }
            // Eksik türleri 0 ile tamamla.
            JokerType.entries.associateWith { type -> byType[type] ?: 0 }
        }

    suspend fun getQuantity(type: JokerType): Int =
        dao.getByType(type.name)?.quantity ?: 0

    /**
     * Envanteri [delta] kadar arttırır (negatif olabilir).
     * Ürün satın alma / joker kullanma akışlarının temel giriş noktası.
     *
     * @return Yeni miktar (asla 0'dan küçük olmaz).
     */
    suspend fun adjust(type: JokerType, delta: Int): Int {
        val current = getQuantity(type)
        val next = (current + delta).coerceAtLeast(0)
        dao.upsert(
            JokerInventoryEntity(
                jokerType = type.name,
                quantity = next,
                updatedAt = System.currentTimeMillis()
            )
        )
        return next
    }

    /** Tek bir joker adetini [quantity]'e sabitler. */
    suspend fun setQuantity(type: JokerType, quantity: Int) {
        dao.upsert(
            JokerInventoryEntity(
                jokerType = type.name,
                quantity = quantity.coerceAtLeast(0),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    /** DB'deki string → enum. Bilinmeyen değerlerde `null` döner (ignore). */
    private fun parse(raw: String): JokerType? =
        runCatching { JokerType.valueOf(raw) }.getOrNull()
}
