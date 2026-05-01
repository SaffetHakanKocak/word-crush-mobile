package com.saffet.wordcrushmobile.ui.components.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saffet.wordcrushmobile.domain.model.SpecialType
import java.util.Locale

/**
 * Tahtadaki tek hücre — modernize edilmiş görsel tasarım.
 *
 * Görsel durumları:
 *  - normal: surface rengi, hafif gölge
 *  - seçili (isSelected): secondaryContainer, belirgin elevation
 *  - son seçilen (isLast): primary rengi, büyük gölge, scale-up
 *  - joker hedef: tertiaryContainer, vurgu kenarlığı
 *  - patlama: error tonu, scale-up animasyonu
 *  - özel güç: tertiary vurgu + rozet
 *
 * Harf değişimi [AnimatedContent] ile fade+scale crossfade yapar.
 * Seçim ve patlama durumları spring animasyonları ile geçiş yapar.
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
    isJokerEffect: Boolean = false,
    effectTranslationX: Float = 0f,
    effectTranslationY: Float = 0f,
    special: SpecialType = SpecialType.NONE,
    clickable: Boolean = true
) {
    val hasSpecial = special != SpecialType.NONE

    // Container rengi: duruma göre animasyonlu geçiş
    val targetContainer: Color = when {
        isExploding   -> MaterialTheme.colorScheme.errorContainer
        isJokerEffect -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f)
        isJokerTarget -> MaterialTheme.colorScheme.tertiaryContainer
        isLast        -> MaterialTheme.colorScheme.primary
        isSelected    -> MaterialTheme.colorScheme.secondaryContainer
        hasSpecial    -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
        else          -> MaterialTheme.colorScheme.surface
    }
    val targetContent: Color = when {
        isExploding   -> MaterialTheme.colorScheme.onErrorContainer
        isJokerEffect -> MaterialTheme.colorScheme.onTertiaryContainer
        isJokerTarget -> MaterialTheme.colorScheme.onTertiaryContainer
        isLast        -> MaterialTheme.colorScheme.onPrimary
        isSelected    -> MaterialTheme.colorScheme.onSecondaryContainer
        hasSpecial    -> MaterialTheme.colorScheme.onTertiaryContainer
        else          -> MaterialTheme.colorScheme.onSurface
    }

    val container by animateColorAsState(
        targetContainer,
        animationSpec = tween(
            durationMillis = CELL_COLOR_MS,
            easing = FastOutSlowInEasing
        ),
        label = "cellContainer"
    )
    val content by animateColorAsState(
        targetContent,
        animationSpec = tween(
            durationMillis = CELL_COLOR_MS,
            easing = FastOutSlowInEasing
        ),
        label = "cellContent"
    )

    // Ölçek animasyonu
    val targetScale = when {
        isExploding   -> 1.24f
        isJokerEffect -> 1.08f
        isLast        -> 1.08f
        isJokerTarget -> 1.06f
        isSelected    -> 1.03f
        else          -> 1f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "cellScale"
    )

    // Elevation animasyonu
    val targetElevation = when {
        isExploding || isLast  -> 8.dp
        isJokerEffect          -> 7.dp
        isSelected             -> 6.dp
        isJokerTarget          -> 4.dp
        hasSpecial             -> 3.dp
        else                   -> 2.dp
    }
    val elevation by animateDpAsState(
        targetValue = targetElevation,
        animationSpec = tween(
            durationMillis = CELL_ELEVATION_MS,
            easing = FastOutSlowInEasing
        ),
        label = "cellElevation"
    )

    // Kenarlık
    val border = when {
        isExploding   -> BorderStroke(2.dp, MaterialTheme.colorScheme.error)
        isJokerEffect -> BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
        isLast        -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        isJokerTarget -> BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
        isSelected    -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
        hasSpecial    -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f))
        else          -> null
    }

    val blastAlpha by animateFloatAsState(
        targetValue = if (isExploding) 0.9f else 0f,
        animationSpec = tween(
            durationMillis = if (isExploding) BLAST_APPEAR_MS else BLAST_RELEASE_MS,
            easing = FastOutSlowInEasing
        ),
        label = "cellBlastAlpha"
    )
    val blastScale by animateFloatAsState(
        targetValue = if (isExploding) 1.42f else 0.82f,
        animationSpec = tween(
            durationMillis = BLAST_SCALE_MS,
            easing = FastOutSlowInEasing
        ),
        label = "cellBlastScale"
    )
    val movingForJoker = effectTranslationX != 0f || effectTranslationY != 0f
    val effectX by animateFloatAsState(
        targetValue = effectTranslationX,
        animationSpec = tween(
            durationMillis = if (movingForJoker) JOKER_EFFECT_MOVE_MS else JOKER_EFFECT_SETTLE_MS,
            delayMillis = if (movingForJoker) JOKER_EFFECT_HOLD_MS else 0,
            easing = FastOutSlowInEasing
        ),
        label = "cellJokerEffectX"
    )
    val effectY by animateFloatAsState(
        targetValue = effectTranslationY,
        animationSpec = tween(
            durationMillis = if (movingForJoker) JOKER_EFFECT_MOVE_MS else JOKER_EFFECT_SETTLE_MS,
            delayMillis = if (movingForJoker) JOKER_EFFECT_HOLD_MS else 0,
            easing = FastOutSlowInEasing
        ),
        label = "cellJokerEffectY"
    )
    val jokerGlowAlpha by animateFloatAsState(
        targetValue = if (isJokerEffect) 0.7f else 0f,
        animationSpec = tween(
            durationMillis = if (isJokerEffect) JOKER_GLOW_APPEAR_MS else JOKER_GLOW_RELEASE_MS,
            easing = FastOutSlowInEasing
        ),
        label = "cellJokerGlowAlpha"
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                translationX = effectX
                translationY = effectY
            }
            .scale(scale),
        onClick = if (clickable) onClick else ({}),
        enabled = clickable,
        shape = RoundedCornerShape(14.dp),
        color = container,
        contentColor = content,
        tonalElevation = elevation,
        shadowElevation = elevation,
        border = border
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (blastAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = blastAlpha
                            scaleX = blastScale
                            scaleY = blastScale
                        }
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.18f))
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(16.dp)
                        )
                )
            }
            if (jokerGlowAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = jokerGlowAlpha
                            scaleX = 1.12f
                            scaleY = 1.12f
                        }
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(16.dp)
                        )
                )
            }

            // Merkezdeki harf
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = letter,
                    transitionSpec = {
                        (
                            slideInVertically(
                                animationSpec = tween(
                                    durationMillis = LETTER_DROP_MS,
                                    easing = FastOutSlowInEasing
                                ),
                                initialOffsetY = { -it * 2 }
                            ) + fadeIn(tween(LETTER_FADE_IN_MS)) + scaleIn(
                                initialScale = 0.82f,
                                animationSpec = tween(
                                    durationMillis = LETTER_SCALE_IN_MS,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        ) togetherWith
                            (
                                slideOutVertically(
                                    animationSpec = tween(
                                        durationMillis = LETTER_OUT_MS,
                                        easing = FastOutSlowInEasing
                                    ),
                                    targetOffsetY = { it / 2 }
                                ) + fadeOut(tween(LETTER_OUT_MS)) + scaleOut(
                                    targetScale = 0.78f,
                                    animationSpec = tween(
                                        durationMillis = LETTER_OUT_MS,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                            )
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

            // Özel güç rozeti: sağ üst köşe
            AnimatedContent(
                targetState = special,
                transitionSpec = {
                    (fadeIn(tween(200)) + scaleIn(
                        initialScale = 0.2f,
                        animationSpec = tween(
                            durationMillis = SPECIAL_BADGE_IN_MS,
                            easing = FastOutSlowInEasing
                        )
                    )) togetherWith fadeOut(tween(SPECIAL_BADGE_OUT_MS))
                },
                label = "cellSpecialBadge",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
            ) { type ->
                if (type != SpecialType.NONE) {
                    SpecialBadge(type = type)
                }
            }
        }
    }
}

/**
 * Küçük yuvarlak rozet: özel güç sembolü.
 */
@Composable
private fun SpecialBadge(type: SpecialType) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
        shadowElevation = 3.dp,
        modifier = Modifier.size(18.dp)
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

private const val CELL_COLOR_MS: Int = 260
private const val CELL_ELEVATION_MS: Int = 260
private const val BLAST_APPEAR_MS: Int = 140
private const val BLAST_RELEASE_MS: Int = 360
private const val BLAST_SCALE_MS: Int = 380
private const val LETTER_DROP_MS: Int = 420
private const val LETTER_FADE_IN_MS: Int = 280
private const val LETTER_SCALE_IN_MS: Int = 360
private const val LETTER_OUT_MS: Int = 220
private const val SPECIAL_BADGE_IN_MS: Int = 280
private const val SPECIAL_BADGE_OUT_MS: Int = 220
private const val JOKER_EFFECT_HOLD_MS: Int = 150
private const val JOKER_EFFECT_MOVE_MS: Int = 520
private const val JOKER_EFFECT_SETTLE_MS: Int = 220
private const val JOKER_GLOW_APPEAR_MS: Int = 180
private const val JOKER_GLOW_RELEASE_MS: Int = 360
