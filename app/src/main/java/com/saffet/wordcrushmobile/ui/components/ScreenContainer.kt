package com.saffet.wordcrushmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.saffet.wordcrushmobile.ui.theme.WordCrushTheme

@Composable
fun GradientBackground(modifier: Modifier = Modifier) {
    val colors = listOf(
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        MaterialTheme.colorScheme.background
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = colors,
                    start = Offset.Zero,
                    end = Offset(1200f, 1800f)
                )
            )
    )
}

@Composable
fun ScreenContainer(
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = WordCrushTheme.spacing.lg,
        vertical = WordCrushTheme.spacing.md
    ),
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        GradientBackground()
        val bodyModifier = if (scrollable) {
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
        } else {
            Modifier
                .fillMaxSize()
                .padding(contentPadding)
        }
        Box(modifier = bodyModifier) {
            content()
        }
    }
}
