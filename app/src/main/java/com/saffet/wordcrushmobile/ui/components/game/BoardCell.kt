package com.saffet.wordcrushmobile.ui.components.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saffet.wordcrushmobile.domain.model.SpecialType
import java.util.Locale

/**
 * Tahtadaki tek hücre. Tıklanabilir; görsel durumları:
 *  - normal / seçili / "son seçilen"
 *  - aktif joker hedef modu
 *  - "patlama" anı (ViewModel'den gelen kısa vurgu)
 *  - **özel güç taşıyor**: hücre kenarlığı vurgulu çizilir ve sağ üst
 *    köşeye PDF §6 tablosundaki gibi küçük bir rozet bırakılır.
 *    Harf aynen ortada kalmaya devam eder, rozet ek katmandır.
 *
 * Harf değişimi [AnimatedContent] ile fade+scale crossfade yapar; yani
 * gravity sonrası hücreye yeni harf gelirse geçiş gözle fark edilir.
 */
@Composable
fun BoardCell(
    letter: Char,
    isSelected: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isJokerTarget: Boolean = false,
    isExploding: Boolean = false,
    special: SpecialType = SpecialType.NONE,
    /**
     * Surface'in `onClick`'ının etkin olup olmadığı. Drag tabanlı seçim
     * aktifken (`GameBoard.enableDrag = true`) tap davranışı devre dışı
     * bırakılır — tüm pointer olayları parent gesture layer'ı tarafından
     * yönetilir. Joker targeting modunda tap tekrar açılır.
     */
    clickable: Boolean = true
) {
    val hasSpecial = special != SpecialType.NONE

    val targetContainer: Color = when {
        isExploding   -> MaterialTheme.colorScheme.errorContainer
        isJokerTarget -> MaterialTheme.colorScheme.tertiaryContainer
        isLast        -> MaterialTheme.colorScheme.primary
        isSelected    -> MaterialTheme.colorScheme.secondaryContainer
        // Özel hücre → sürekli görünür olsun diye hafif tertiary tonu.
        hasSpecial    -> MaterialTheme.colorScheme.tertiaryContainer
        else          -> MaterialTheme.colorScheme.surface
    }
    val targetContent: Color = when {
        isExploding   -> MaterialTheme.colorScheme.onErrorContainer
        isJokerTarget -> MaterialTheme.colorScheme.onTertiaryContainer
        isLast        -> MaterialTheme.colorScheme.onPrimary
        isSelected    -> MaterialTheme.colorScheme.onSecondaryContainer
        hasSpecial    -> MaterialTheme.colorScheme.onTertiaryContainer
        else          -> MaterialTheme.colorScheme.onSurface
    }

    val container by animateColorAsState(targetContainer, label = "cellContainer")
    val content by animateColorAsState(targetContent, label = "cellContent")

    val targetScale = when {
        isExploding -> 1.12f
        isLast      -> 1.05f
        else        -> 1f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 180),
        label = "cellScale"
    )

    Surface(
        modifier = modifier.scale(scale),
        onClick = if (clickable) onClick else ({}),
        enabled = clickable,
        shape = RoundedCornerShape(10.dp),
        color = container,
        contentColor = content,
        tonalElevation = if (isSelected || isJokerTarget || isExploding || hasSpecial) 4.dp else 1.dp,
        shadowElevation = if (isSelected || isJokerTarget || isExploding || hasSpecial) 4.dp else 1.dp,
        border = when {
            isExploding   -> BorderStroke(2.dp, MaterialTheme.colorScheme.error)
            isJokerTarget -> BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
            hasSpecial    -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary)
            else          -> null
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Merkezdeki harf: her zaman çizilir. Gravity sonrası harf
            // değişimini crossfade ile animate ederiz ki "yeni harf geldi"
            // hissi oluşsun.
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = letter,
                    transitionSpec = {
                        (fadeIn(tween(180)) + scaleIn(
                            initialScale = 0.6f,
                            animationSpec = tween(180)
                        )) togetherWith
                            (fadeOut(tween(140)) + scaleOut(
                                targetScale = 0.6f,
                                animationSpec = tween(140)
                            ))
                    },
                    label = "cellLetter"
                ) { shown ->
                    Text(
                        text = shown.toString().uppercase(Locale.forLanguageTag("tr-TR")),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Özel güç rozeti: sağ üst köşe. Harfin üzerinde küçük yuvarlak
            // bir etikettir; PDF §6 tablosundaki sembollerle eşlenir.
            // AnimatedContent ile tip değiştiğinde (örn. bırakıldığı anda)
            // küçük bir fade-in yapar; silinip NONE olduğunda fade-out'la
            // kaybolur.
            AnimatedContent(
                targetState = special,
                transitionSpec = {
                    (fadeIn(tween(200)) + scaleIn(
                        initialScale = 0.2f,
                        animationSpec = tween(200)
                    )) togetherWith fadeOut(tween(140))
                },
                label = "cellSpecialBadge",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
            ) { type ->
                if (type != SpecialType.NONE) {
                    SpecialBadge(type = type)
                }
            }
        }
    }
}

/**
 * Küçük (16.dp) yuvarlak rozet. İçinde PDF §6 tablosundaki sembol yer alır.
 * Sembolleri yaygın Unicode karakterleriyle seçtik; böylece ek font
 * asset'i gerekmez ve `material-icons-core` setine bağımlılık kalmaz:
 *
 *  - ROW_CLEAR    → ↔ (U+2194) — yatay ok, satır temizleme
 *  - COLUMN_CLEAR → ↕ (U+2195) — dikey ok, sütun temizleme
 *  - AREA_BLAST   → ✦ (U+2726) — dört uçlu yıldız, alan patlatma
 *  - MEGA_BLAST   → ★ (U+2605) — dolu yıldız, mega patlatma
 */
@Composable
private fun SpecialBadge(type: SpecialType) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
        shadowElevation = 2.dp,
        modifier = Modifier.size(16.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbolFor(type),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

private fun symbolFor(type: SpecialType): String = when (type) {
    SpecialType.NONE         -> ""
    SpecialType.ROW_CLEAR    -> "↔"
    SpecialType.COLUMN_CLEAR -> "↕"
    SpecialType.AREA_BLAST   -> "✦"
    SpecialType.MEGA_BLAST   -> "★"
}
