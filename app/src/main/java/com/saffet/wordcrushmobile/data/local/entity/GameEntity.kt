package com.saffet.wordcrushmobile.data.local.entity

/**
 * Veritabanında saklanacak bir oyun kaydını temsil eder.
 * İleride Room @Entity anotasyonu ile işaretlenecektir.
 */
data class GameEntity(
    val id: Long = 0L,
    val score: Int = 0,
    val level: Int = 1,
    val createdAt: Long = 0L
)
