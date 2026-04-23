package com.saffet.wordcrushmobile.ui.components.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Şu an oluşmakta olan kelimeyi gösteren modern bant.
 *
 * Kelime boşken placeholder metin gösterilir. Kelime oluşurken her harf
 * ayrı bir "chip" gibi render edilir; kelime değiştiğinde AnimatedContent
 * ile smooth geçiş yapılır.
 *
 * Gradient arka plan ile oyun hissi güçlendirilir.
 */
@Composable
fun CurrentWordDisplay(
    word: String,
    modifier: Modifier = Modifier
) {
    val hasWord = word.isNotEmpty()

    val bgBrush = if (hasWord) {
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgBrush),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = word,
            transitionSpec = {
                (fadeIn(tween(200)) + scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(200)
                )) togetherWith fadeOut(tween(150))
            },
            label = "wordDisplay"
        ) { currentWord ->
            if (currentWord.isEmpty()) {
                Text(
                    text = "Harflere dokunarak kelime oluştur",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    currentWord.uppercase(Locale.forLanguageTag("tr-TR")).forEach { ch ->
                        LetterChip(letter = ch)
                    }
                }
            }
        }
    }
}

/**
 * Kelime bandındaki tek harf chip'i.
 * Hafif arka plan ile her harf bireysel olarak görünür.
 */
@Composable
private fun LetterChip(letter: Char) {
    Box(
        modifier = Modifier
            .widthIn(min = 28.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.toString(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}
