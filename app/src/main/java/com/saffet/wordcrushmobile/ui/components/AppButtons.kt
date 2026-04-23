package com.saffet.wordcrushmobile.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.saffet.wordcrushmobile.ui.theme.Elevations
import com.saffet.wordcrushmobile.ui.theme.WordCrushTheme

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "primaryButtonScale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .heightIn(min = 52.dp),
        shape = RoundedCornerShape(16.dp),
        interactionSource = interaction,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = Elevations.medium,
            pressedElevation = Elevations.low,
            disabledElevation = Elevations.none
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = WordCrushTheme.spacing.lg,
            vertical = WordCrushTheme.spacing.sm
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        label = "secondaryButtonScale"
    )

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .heightIn(min = 52.dp),
        interactionSource = interaction,
        shape = RoundedCornerShape(16.dp),
        border = ButtonDefaults.outlinedButtonBorder(enabled = enabled),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = WordCrushTheme.spacing.lg,
            vertical = WordCrushTheme.spacing.sm
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}
