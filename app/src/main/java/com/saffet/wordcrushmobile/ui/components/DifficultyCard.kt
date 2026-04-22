package com.saffet.wordcrushmobile.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saffet.wordcrushmobile.domain.model.GameDifficulty

/**
 * Zorluk seçim kartı.
 *
 * Seçili durumda kart, belirgin bir arka plan rengi ve 2 dp kenarlıkla vurgulanır;
 * böylece kullanıcı hangisini seçtiğini tek bakışta anlar. Sağdaki onay ikonu
 * erişilebilirlik açısından da görsel bir işaret sağlar.
 *
 * Not: Material 3'te [ElevatedCard] imzası `border` parametresi almaz (bu yalnızca
 * `OutlinedCard`/`Card` için geçerlidir). Kenarlık efekti, elevation'ı korumak
 * adına [Modifier.border] ile uygulanır.
 *
 * Kart tıklandığında [onClick] tetiklenir — seçim mantığı çağıran tarafın
 * (NewGameScreen / NewGameViewModel) sorumluluğundadır.
 */
@Composable
fun DifficultyCard(
    difficulty: GameDifficulty,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val shape = RoundedCornerShape(20.dp)
    val borderModifier = if (isSelected) {
        Modifier.border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = shape
        )
    } else {
        Modifier
    }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .then(borderModifier),
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = difficulty.label,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "${difficulty.description} · ${difficulty.moves} hamle",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = if (isSelected) "Seçili" else null,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Transparent
                },
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
