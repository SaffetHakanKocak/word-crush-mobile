package com.saffet.wordcrushmobile.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Kullanıcının envanterindeki joker miktarlarını tutar.
 *
 * PK olarak [jokerType] (enum adı, ör. "SHUFFLE") kullanılır — her joker
 * tipi için yalnızca tek satır olur. Böylece `UPSERT` ile hızlıca miktar
 * güncellenir.
 */
@Entity(tableName = "joker_inventory")
data class JokerInventoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "joker_type")
    val jokerType: String,

    /** Envanterdeki adet. 0 olabilir (hiç almamış ya da hepsini kullanmış). */
    val quantity: Int,

    /** Son güncelleme anı (epoch millis) — teşhis amaçlı. */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
