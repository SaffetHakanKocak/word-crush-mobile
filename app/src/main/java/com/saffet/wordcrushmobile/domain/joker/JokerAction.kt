package com.saffet.wordcrushmobile.domain.joker

import com.saffet.wordcrushmobile.domain.engine.BoardPosition
import com.saffet.wordcrushmobile.domain.model.JokerType

/**
 * Tek bir joker kullanımının parametrelerini tipli olarak ifade eden
 * sealed hiyerarşi.
 *
 * Her jokerin ihtiyaç duyduğu hedef sayısı farklıdır (0 / 1 / 2). UI
 * katmanı [JokerType]'a göre kaç hedef toplayacağını [JokerTargetSpec]
 * üzerinden öğrenir ve uygun data class'ı inşa ederek [JokerEngine.apply]
 * fonksiyonuna verir.
 *
 * Sealed hierarşinin avantajı: `when` tarafı [JokerEngine]'de exhaustive
 * kalır; ileride yeni bir joker eklendiğinde derleyici eksik kolları
 * gösterir.
 */
sealed class JokerAction {

    /** Her eylemin hangi joker tipine karşılık geldiği. */
    abstract val type: JokerType

    /**
     * FISH — Rastgele [count] kadar hücreyi grid'den siler. Seçim tamamen
     * tesadüfidir; silinen hücrelerin üstündekiler yerçekimiyle düşer.
     */
    data class Fish(
        val count: Int = DEFAULT_FISH_COUNT
    ) : JokerAction() {
        override val type: JokerType = JokerType.FISH

        init {
            require(count > 0) { "Fish count > 0 olmalı, verilen: $count" }
        }

        companion object {
            /** PDF açık bir sayı vermez; dengeli bir değer seçildi. */
            const val DEFAULT_FISH_COUNT: Int = 5
        }
    }

    /**
     * WHEEL — [target] hücresinin bulunduğu satır ve sütunun tamamını siler.
     */
    data class Wheel(val target: BoardPosition) : JokerAction() {
        override val type: JokerType = JokerType.WHEEL
    }

    /**
     * LOLLIPOP_HAMMER — Yalnızca [target] tek hücresini siler.
     */
    data class Lollipop(val target: BoardPosition) : JokerAction() {
        override val type: JokerType = JokerType.LOLLIPOP_HAMMER
    }

    /**
     * FREE_SWAP — Birbirine **komşu** iki hücrenin (8 yönlü) içeriğini
     * takas eder. Konumlar aynı kalır; hücrelerin harf + özel tipi yer
     * değiştirir.
     *
     * Komşuluk kontrolü [JokerEngine] içinde yapılır; komşu değilse
     * [JokerResult.InvalidTarget] döner.
     */
    data class FreeSwap(
        val a: BoardPosition,
        val b: BoardPosition
    ) : JokerAction() {
        override val type: JokerType = JokerType.FREE_SWAP
    }

    /**
     * LETTER_SHUFFLE — Özel simge taşımayan tüm hücrelerin harflerini
     * rastgele permütasyona tabi tutar. Özel hücreler yerinde kalır
     * (PDF §6 "yerinde kalır" kuralıyla tutarlı).
     */
    data object LetterShuffle : JokerAction() {
        override val type: JokerType = JokerType.LETTER_SHUFFLE
    }

    /**
     * PARTY_BOOSTER — Tüm grid'i yok eder ve yeni rastgele harflerle
     * doldurur. Tüm özel simgeler de kaybolur.
     */
    data object PartyBooster : JokerAction() {
        override val type: JokerType = JokerType.PARTY_BOOSTER
    }
}

/**
 * UI'ın "bu joker kaç hedef gerektiriyor?" sorusuna cevabı.
 */
data class JokerTargetSpec(
    val type: JokerType,
    val neededTargets: Int,
    /** FREE_SWAP gibi hedefler birbirine komşu olmak zorunda mı? */
    val requiresAdjacentTargets: Boolean = false
) {
    companion object {
        fun of(type: JokerType): JokerTargetSpec = when (type) {
            JokerType.FISH            -> JokerTargetSpec(type, neededTargets = 0)
            JokerType.LETTER_SHUFFLE  -> JokerTargetSpec(type, neededTargets = 0)
            JokerType.PARTY_BOOSTER   -> JokerTargetSpec(type, neededTargets = 0)
            JokerType.WHEEL           -> JokerTargetSpec(type, neededTargets = 1)
            JokerType.LOLLIPOP_HAMMER -> JokerTargetSpec(type, neededTargets = 1)
            JokerType.FREE_SWAP       -> JokerTargetSpec(
                type = type,
                neededTargets = 2,
                requiresAdjacentTargets = true
            )
        }
    }
}
