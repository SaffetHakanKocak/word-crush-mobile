package com.saffet.wordcrushmobile.ui.components.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

/**
 * Tahtadaki tek hücre. Tıklanabilir, seçili/seçilmemiş/son-seçilen
 * durumlarına göre renk değiştirir.
 *
 * Sürükleme yerine sade tıklama akışı kullanır (ilk sürüm gereksinimi).
 *
 * @param letter        Hücrede gösterilecek harf.
 * @param isSelected    Hücre mevcut seçim zincirinde mi?
 * @param isLast        Seçim zincirinin son elemanı mı? (farklı vurgu için)
 * @param isJokerTarget Hücre aktif joker hedef modunda seçilmiş mi?
 *                      (ör. FreeSwap'te 2. hedef beklenirken 1. kart).
 * @param onClick       Tıklama olayı; ViewModel'e iletilir.
 */
@Composable
fun BoardCell(
    letter: Char,
    isSelected: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isJokerTarget: Boolean = false
) {
    val targetContainer: Color = when {
        isJokerTarget -> MaterialTheme.colorScheme.tertiaryContainer
        isLast        -> MaterialTheme.colorScheme.primary
        isSelected    -> MaterialTheme.colorScheme.secondaryContainer
        else          -> MaterialTheme.colorScheme.surface
    }
    val targetContent: Color = when {
        isJokerTarget -> MaterialTheme.colorScheme.onTertiaryContainer
        isLast        -> MaterialTheme.colorScheme.onPrimary
        isSelected    -> MaterialTheme.colorScheme.onSecondaryContainer
        else          -> MaterialTheme.colorScheme.onSurface
    }

    val container by animateColorAsState(targetContainer, label = "cellContainer")
    val content by animateColorAsState(targetContent, label = "cellContent")

    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = container,
        contentColor = content,
        tonalElevation = if (isSelected || isJokerTarget) 4.dp else 1.dp,
        shadowElevation = if (isSelected || isJokerTarget) 4.dp else 1.dp,
        border = if (isJokerTarget)
            BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
        else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter.toString().uppercase(Locale.forLanguageTag("tr-TR")),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
