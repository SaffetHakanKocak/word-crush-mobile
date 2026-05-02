package com.saffet.wordcrushmobile.ui.screens.market

import android.view.SoundEffectConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saffet.wordcrushmobile.R
import com.saffet.wordcrushmobile.domain.model.JokerType
import com.saffet.wordcrushmobile.ui.components.BackgroundImageLayer
import com.saffet.wordcrushmobile.ui.components.game.JokerIcon
import com.saffet.wordcrushmobile.viewmodel.MarketUiState
import com.saffet.wordcrushmobile.viewmodel.MarketViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Joker market ekrani.
 *
 * Dokumandaki beklentiye uygun olarak her joker icin ozellik aciklamasi,
 * kullanim amaci, altin maliyeti, kullanim sekli ve hafif bir onizleme alani
 * gosterir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    onBack: () -> Unit,
    viewModel: MarketViewModel = viewModel(factory = MarketViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    LaunchedEffect(Unit) {
        viewModel.messages.collect { msg ->
            if (msg.contains("yetersiz", ignoreCase = true)) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                view.playSoundEffect(SoundEffectConstants.CLICK)
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                view.playSoundEffect(SoundEffectConstants.CLICK)
            }
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0.dp),
                title = { Text("Market", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(14.dp),
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BackgroundImageLayer(
                drawableRes = R.drawable.market_bg,
                overlayBrush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.46f)
                    )
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.04f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.scrim.copy(alpha = 0.08f)
                            )
                        )
                    )
            )

            if (state.isLoading) {
                LoadingState()
            } else {
                MarketContent(
                    state = state,
                    onPurchase = viewModel::purchase
                )
            }
        }
    }
}

@Composable
private fun MarketContent(
    state: MarketUiState,
    onPurchase: (JokerType) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "gold-header") {
            GoldHeaderCard(gold = state.gold)
        }

        item(key = "section-title") {
            Column(
                modifier = Modifier.padding(start = 4.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Güçlendiriciler",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Text(
                    text = "Her jokerin ne yaptığını, ne zaman avantaj sağladığını ve oyunda nasıl tetikleneceğini buradan görebilirsin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        itemsIndexed(
            items = JokerType.entries.toList(),
            key = { _, it -> it.name }
        ) { index, type ->
            val visibleState = remember { MutableTransitionState(false) }
            LaunchedEffect(Unit) {
                visibleState.targetState = true
            }

            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(tween(300, delayMillis = (index * 60).coerceAtMost(600))) +
                    slideInVertically(
                        initialOffsetY = { 60 },
                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                    )
            ) {
                JokerCard(
                    type = type,
                    owned = state.ownedOf(type),
                    canAfford = state.canAfford(type),
                    isPurchasing = state.isPurchasing,
                    onBuy = { onPurchase(type) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun GoldHeaderCard(gold: Int) {
    val brush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.82f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(brush)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Mevcut Bakiye",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = "\uD83E\uDE99 $gold Altın",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\uD83E\uDE99",
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun JokerCard(
    type: JokerType,
    owned: Int,
    canAfford: Boolean,
    isPurchasing: Boolean,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    JokerIcon(
                        type = type,
                        size = 38.dp
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = type.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        OwnedBadge(count = owned)
                    }

                    UsagePill(text = type.usageMethod)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                JokerInfoBlock(
                    title = "Özellik Açıklaması",
                    text = type.description
                )
                JokerInfoBlock(
                    title = "Kullanım Amacı",
                    text = type.purpose
                )
                JokerInfoBlock(
                    title = "Kullanım Şekli",
                    text = type.usageMethod,
                    emphasize = true
                )
            }

            PreviewPanel(type = type)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriceTag(cost = type.costGold, canAfford = canAfford)

                Button(
                    onClick = onBuy,
                    enabled = canAfford && !isPurchasing,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = if (canAfford) "Satın Al" else "Yetersiz",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewPanel(type: JokerType) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Nasıl kullanılır?",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JokerPreview(type = type)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PreviewHint(type = type)
                Text(
                    text = type.usageMethod,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun JokerPreview(type: JokerType) {
    val transition = rememberInfiniteTransition(label = "${type.name}Preview")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "${type.name}Progress"
    )

    Box(
        modifier = Modifier
            .size(width = 112.dp, height = 84.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        when (type) {
            JokerType.FISH -> FishPreview(progress)
            JokerType.WHEEL -> WheelPreview(progress)
            JokerType.LOLLIPOP_HAMMER -> LollipopPreview(progress)
            JokerType.FREE_SWAP -> SwapPreview(progress)
            JokerType.LETTER_SHUFFLE -> ShufflePreview(progress)
            JokerType.PARTY_BOOSTER -> PartyPreview(progress)
        }
    }
}

@Composable
private fun FishPreview(progress: Float) {
    PreviewGrid { row, col ->
        val pulse = when {
            row == 0 && col == 2 -> pulse(progress, 0.08f)
            row == 1 && col == 1 -> pulse(progress, 0.36f)
            row == 2 && col == 0 -> pulse(progress, 0.64f)
            else -> 0f
        }
        PreviewCell(
            highlighted = pulse > 0.35f,
            alpha = 0.45f + pulse * 0.55f,
            scale = 1f + pulse * 0.18f
        )
    }
}

@Composable
private fun WheelPreview(progress: Float) {
    PreviewGrid { row, col ->
        val selectPulse = pulse(progress, 0.06f)
        val crossPulse = pulse(progress, 0.34f)
        val clearPulse = pulse(progress, 0.64f)
        val cross = row == 1 || col == 1
        val center = row == 1 && col == 1

        PreviewCell(
            highlighted = center || (cross && crossPulse > 0.2f),
            alpha = when {
                center -> 0.55f + selectPulse * 0.35f + crossPulse * 0.1f - clearPulse * 0.55f
                cross -> 0.3f + crossPulse * 0.6f - clearPulse * 0.55f
                else -> 0.3f
            }.coerceIn(0.12f, 0.95f),
            scale = when {
                center -> 1f + selectPulse * 0.18f
                cross -> 1f + crossPulse * 0.08f
                else -> 1f
            }
        )
    }
}

@Composable
private fun LollipopPreview(progress: Float) {
    PreviewGrid { row, col ->
        val selected = row == 1 && col == 1
        val vanish = if (selected) 1f - wave(progress, 0.18f) else 0f
        PreviewCell(
            highlighted = selected,
            alpha = if (selected) 0.2f + vanish * 0.8f else 0.42f,
            scale = if (selected) 0.75f + vanish * 0.25f else 1f
        )
    }
}

@Composable
private fun SwapPreview(progress: Float) {
    val selectLeft = pulse(progress, 0.06f)
    val selectRight = pulse(progress, 0.22f)
    val swapPhase = wave(progress, 0.5f)
    val leftToRight = swapPhase * 20f
    val rightToLeft = -swapPhase * 20f

    Box(contentAlignment = Alignment.Center) {
        PreviewGrid { row, col ->
            val leftSelected = row == 1 && col == 0
            val rightSelected = row == 1 && col == 1
            when {
                leftSelected -> PreviewCell(
                    highlighted = true,
                    alpha = 0.7f + selectLeft * 0.2f + swapPhase * 0.1f,
                    translationX = leftToRight
                )
                rightSelected -> PreviewCell(
                    highlighted = true,
                    alpha = 0.7f + selectRight * 0.2f + swapPhase * 0.1f,
                    translationX = rightToLeft
                )
                else -> PreviewCell(alpha = 0.4f)
            }
        }

        if (swapPhase > 0.08f) {
            Text(
                text = "\u2194",
                modifier = Modifier.offset {
                    IntOffset(0, (-14f * swapPhase).roundToInt())
                },
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ShufflePreview(progress: Float) {
    val wholeGridPulse = pulse(progress, 0.02f)
    PreviewGrid { row, col ->
        val angle = progress * 2f * PI
        val isAnimated = row != 1 || col != 1
        if (isAnimated) {
            val offsetSeed = when {
                row == 0 && col == 0 -> 0.0
                row == 0 && col == 1 -> PI / 4
                row == 0 && col == 2 -> PI / 2
                row == 1 && col == 0 -> PI * 0.75
                row == 1 && col == 2 -> PI * 1.1
                row == 2 && col == 0 -> PI * 1.35
                row == 2 && col == 1 -> PI * 1.6
                else -> PI * 1.9
            }
            PreviewCell(
                highlighted = wholeGridPulse > 0.22f,
                alpha = 0.62f + wholeGridPulse * 0.24f,
                translationX = (cos(angle + offsetSeed) * 8.5).toFloat(),
                translationY = (sin(angle + offsetSeed) * 8.5).toFloat()
            )
        } else {
            PreviewCell(
                highlighted = wholeGridPulse > 0.22f,
                alpha = 0.55f + wholeGridPulse * 0.18f,
                scale = 1f + wholeGridPulse * 0.05f
            )
        }
    }
}

@Composable
private fun PartyPreview(progress: Float) {
    PreviewGrid { row, col ->
        val stagger = ((row * 3 + col) * 0.05f)
        val cycle = ((progress + stagger) % 1f)
        val alpha = if (cycle < 0.45f) {
            0.9f - cycle * 1.6f
        } else {
            0.25f + (cycle - 0.45f) * 1.2f
        }.coerceIn(0.2f, 0.92f)
        PreviewCell(
            highlighted = alpha > 0.7f,
            alpha = alpha,
            scale = 0.94f + alpha * 0.08f
        )
    }
}

@Composable
private fun PreviewGrid(
    content: @Composable (row: Int, col: Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) { col ->
                    content(row, col)
                }
            }
        }
    }
}

@Composable
private fun PreviewCell(
    highlighted: Boolean = false,
    alpha: Float = 1f,
    scale: Float = 1f,
    translationX: Float = 0f,
    translationY: Float = 0f
) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
                this.translationX = translationX
                this.translationY = translationY
            }
            .clip(RoundedCornerShape(5.dp))
            .background(
                if (highlighted) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                }
            )
    )
}

@Composable
private fun JokerInfoBlock(
    title: String,
    text: String,
    emphasize: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (emphasize) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (emphasize) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun UsagePill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun PreviewHint(type: JokerType) {
    val text = when (type) {
        JokerType.FISH -> "Rastgele harfleri siler"
        JokerType.WHEEL -> "1 hücre seç \u2192 satır ve sütun temizlenir"
        JokerType.LOLLIPOP_HAMMER -> "1 harf seç \u2192 silinir"
        JokerType.FREE_SWAP -> "2 komşu hücre seç \u2192 yer değiştir"
        JokerType.LETTER_SHUFFLE -> "Tüm grid karıştırılır"
        JokerType.PARTY_BOOSTER -> "Tüm grid temizlenir ve yeniden dolar"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun OwnedBadge(count: Int) {
    val bgColor = if (count > 0) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (count > 0) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "Sahip Olunan: $count",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun PriceTag(cost: Int, canAfford: Boolean) {
    val color = if (canAfford) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "\uD83E\uDE99 $cost Altın",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

private fun pulse(progress: Float, start: Float): Float {
    val local = ((progress - start + 1f) % 1f)
    return when {
        local < 0.18f -> local / 0.18f
        local < 0.36f -> 1f - ((local - 0.18f) / 0.18f)
        else -> 0f
    }
}

private fun wave(progress: Float, start: Float): Float {
    val local = ((progress - start + 1f) % 1f)
    return when {
        local < 0.5f -> local / 0.5f
        else -> 1f - ((local - 0.5f) / 0.5f)
    }.coerceIn(0f, 1f)
}
