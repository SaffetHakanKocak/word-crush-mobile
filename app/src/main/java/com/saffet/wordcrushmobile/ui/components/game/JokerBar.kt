package com.saffet.wordcrushmobile.ui.components.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saffet.wordcrushmobile.domain.model.JokerType

/**
 * Oyun ekranının altında yer alan modern joker barı.
 *
 * LazyRow ile yatay kaydırılabilir. Her joker ikonlu, adlı ve durum
 * göstergeli compact kartlar şeklinde gösterilir.
 *
 * Seçili joker vurgulu border + scale animasyonu alır.
 */
@Composable
fun JokerBar(
    inventory: Map<JokerType, Int>,
    selectedType: JokerType?,
    onJokerClick: (JokerType) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = JokerType.entries.toList()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .padding(vertical = 8.dp)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(items = items, key = { it.name }) { type ->
                JokerCard(
                    type = type,
                    quantity = inventory[type] ?: 0,
                    isSelected = selectedType == type,
                    onClick = { onJokerClick(type) }
                )
            }
        }
    }
}

/**
 * Tek bir joker kartı — modern, compact tasarım.
 *
 * Seçili durumda:
 *  - Tertiary border ile vurgulanır
 *  - Scale-up animasyonu alır
 *  - Container rengi değişir
 *
 * Adet 0 ise kart sadece opaklıkla farklılaşır.
 */
@Composable
private fun JokerCard(
    type: JokerType,
    quantity: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val owned = quantity > 0

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "jokerCardScale"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.tertiaryContainer
            owned      -> MaterialTheme.colorScheme.surfaceVariant
            else       -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(250),
        label = "jokerContainerColor"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.onTertiaryContainer
            owned      -> MaterialTheme.colorScheme.onSurfaceVariant
            else       -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(250),
        label = "jokerContentColor"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(88.dp)
            .scale(scale),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 1.dp
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = iconFor(type),
                contentDescription = type.displayName,
                modifier = Modifier.size(28.dp),
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.tertiary
                } else if (owned) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }
            )

            Text(
                text = type.displayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )

            QuantityBadge(quantity = quantity, dimmed = !owned)
        }
    }
}

/**
 * Adet göstergesi. Modern pill badge.
 */
@Composable
private fun QuantityBadge(quantity: Int, dimmed: Boolean) {
    val text = if (quantity > 0) "x$quantity" else "–"

    val bgColor by animateColorAsState(
        targetValue = if (dimmed) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(250),
        label = "badgeBg"
    )
    val fgColor by animateColorAsState(
        targetValue = if (dimmed) {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.onPrimary
        },
        animationSpec = tween(250),
        label = "badgeFg"
    )

    Surface(
        shape = CircleShape,
        color = bgColor,
        contentColor = fgColor
    ) {
        Box(
            modifier = Modifier
                .height(20.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Joker tipine göre Material simge eşlemesi.
 */
private fun iconFor(type: JokerType): ImageVector = when (type) {
    JokerType.FISH            -> Icons.Filled.Favorite
    JokerType.WHEEL           -> Icons.Filled.LocationOn
    JokerType.LOLLIPOP_HAMMER -> Icons.Filled.Clear
    JokerType.FREE_SWAP       -> Icons.AutoMirrored.Filled.ArrowForward
    JokerType.LETTER_SHUFFLE  -> Icons.Filled.Refresh
    JokerType.PARTY_BOOSTER   -> Icons.Filled.Star
}
