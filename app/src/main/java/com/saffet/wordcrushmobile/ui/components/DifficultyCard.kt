package com.saffet.wordcrushmobile.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saffet.wordcrushmobile.domain.model.GameDifficulty

/**
 * Modern zorluk seçim kartı.
 *
 * Seçili durumda kart:
 *  - Primary border ile vurgulanır
 *  - Hafif scale-up animasyonu alır
 *  - Elevation artışı gösterir
 *  - Onay ikonu animasyonlu olarak görünür
 *
 * Her kart grid boyutunu, hamle sayısını ve zorluk seviyesini
 * modern badge'ler ile gösterir.
 */
@Composable
fun DifficultyCard(
    difficulty: GameDifficulty,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animated values
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "difficultyScale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        animationSpec = tween(300),
        label = "difficultyElevation"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(300),
        label = "difficultyContainerColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        },
        animationSpec = tween(300),
        label = "difficultyBorderColor"
    )
    val checkAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(250),
        label = "checkAlpha"
    )

    // Difficulty badge color mapping
    val badgeColor = when (difficulty) {
        GameDifficulty.EASY -> MaterialTheme.colorScheme.secondary
        GameDifficulty.MEDIUM -> MaterialTheme.colorScheme.tertiary
        GameDifficulty.HARD -> MaterialTheme.colorScheme.error
    }

    val shape = RoundedCornerShape(20.dp)

    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = shape
            ),
        shape = shape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Grid boyutu kutusu — mini grid önizlemesi
            GridPreviewBox(
                rows = difficulty.rows,
                badgeColor = badgeColor
            )

            Spacer(Modifier.width(16.dp))

            // Ana içerik: başlık + bilgi satırları
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Grid boyutu başlığı
                Text(
                    text = difficulty.label,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Stat badges row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Zorluk seviyesi badge
                    StatBadge(
                        text = difficulty.description,
                        containerColor = badgeColor.copy(alpha = 0.15f),
                        contentColor = badgeColor
                    )
                    // Hamle sayısı badge
                    StatBadge(
                        text = "${difficulty.moves} hamle",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Seçim onay ikonu
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = if (isSelected) "Seçili" else null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = checkAlpha),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * Grid boyutu için mini önizleme kutusu.
 * Grid boyutunu büyük punto ile gösterir, altta renk çubuğu vardır.
 */
@Composable
private fun GridPreviewBox(
    rows: Int,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Grid icon: small dots grid
            Text(
                text = "▦",
                fontSize = 22.sp,
                color = badgeColor
            )
            Spacer(Modifier.height(2.dp))
            // Color indicator bar
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.7f))
            )
        }
    }
}

/**
 * Küçük istatistik badge'i. Zorluk seviyesi ve hamle sayısını
 * yumuşak arka planlı etiketlerle gösterir.
 */
@Composable
private fun StatBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = contentColor
        )
    }
}
