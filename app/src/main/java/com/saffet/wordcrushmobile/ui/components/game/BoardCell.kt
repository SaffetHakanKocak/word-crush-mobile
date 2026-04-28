package com.saffet.wordcrushmobile.ui.components.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
    special: SpecialType = SpecialType.NONE,
    clickable: Boolean = true
) {
    val hasSpecial = special != SpecialType.NONE

    // Container rengi: duruma göre animasyonlu geçiş
    val targetContainer: Color = when {
        isExploding   -> MaterialTheme.colorScheme.errorContainer
        isJokerTarget -> MaterialTheme.colorScheme.tertiaryContainer
        isLast        -> MaterialTheme.colorScheme.primary
        isSelected    -> MaterialTheme.colorScheme.secondaryContainer
        hasSpecial    -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
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

    val container by animateColorAsState(
        targetContainer,
        animationSpec = tween(200),
        label = "cellContainer"
    )
    val content by animateColorAsState(
        targetContent,
        animationSpec = tween(200),
        label = "cellContent"
    )

    // Ölçek animasyonu
    val targetScale = when {
        isExploding -> 1.15f
        isLast      -> 1.08f
        isSelected  -> 1.03f
        else        -> 1f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "cellScale"
    )

    // Elevation animasyonu
    val targetElevation = when {
        isExploding || isLast  -> 8.dp
        isSelected             -> 6.dp
        isJokerTarget          -> 4.dp
        hasSpecial             -> 3.dp
        else                   -> 2.dp
    }
    val elevation by animateDpAsState(
        targetValue = targetElevation,
        animationSpec = tween(200),
        label = "cellElevation"
    )

    // Kenarlık
    val border = when {
        isExploding   -> BorderStroke(2.dp, MaterialTheme.colorScheme.error)
        isLast        -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        isJokerTarget -> BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
        isSelected    -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
        hasSpecial    -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f))
        else          -> null
    }

    Surface(
        modifier = modifier.scale(scale),
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
            // Merkezdeki harf
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

            // Özel güç rozeti: sağ üst köşe
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
