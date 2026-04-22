package com.saffet.wordcrushmobile.domain.engine

import com.saffet.wordcrushmobile.domain.model.Cell
import com.saffet.wordcrushmobile.domain.model.SpecialType

/**
 * Son harfin yerinde kalıp [type] özel simgesine dönüşeceğini ifade eden
 * yerleştirme bilgisi. UI bu konuma özel simge animasyonu uygulayabilir.
 */
data class SpecialPlacement(
    val row: Int,
    val col: Int,
    val type: SpecialType
)

/**
 * [WordCrushEngine.applyWord] çıktısı.
 *
 * @property newBoard           Güncellenmiş tahta (collapse + refill sonrası).
 * @property removedPositions   Bu hamlede patlayan tüm hücrelerin pozisyonları.
 *                              UI animasyonu, skor bonusu veya istatistik için
 *                              kullanılabilir. Son-harf-korunan hücreyi
 *                              **içermez**.
 * @property triggeredSpecials  Bu hamlede aktive olan özel güç tipleri.
 *                              Aynı tip birden fazla gelebilir (ör. iki ayrı
 *                              özel hücre aynı kelimede kullanılmışsa).
 * @property plantedSpecial     Kelime yeterince uzunsa, son harfin yerinde
 *                              bırakılan özel simgenin konumu + tipi.
 *                              Yoksa `null`.
 */
data class ApplyWordResult(
    val newBoard: List<List<Cell>>,
    val removedPositions: Set<BoardPosition>,
    val triggeredSpecials: List<SpecialType>,
    val plantedSpecial: SpecialPlacement?
)
