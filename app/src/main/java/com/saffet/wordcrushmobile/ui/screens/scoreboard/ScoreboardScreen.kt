package com.saffet.wordcrushmobile.ui.screens.scoreboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saffet.wordcrushmobile.R
import com.saffet.wordcrushmobile.domain.model.GameRecord
import com.saffet.wordcrushmobile.domain.model.GameStats
import com.saffet.wordcrushmobile.ui.components.AppTopBar
import com.saffet.wordcrushmobile.ui.components.BackgroundImageLayer
import com.saffet.wordcrushmobile.ui.components.EmptyStateView
import com.saffet.wordcrushmobile.ui.components.LoadingView
import com.saffet.wordcrushmobile.viewmodel.ScoreboardUiState
import com.saffet.wordcrushmobile.viewmodel.ScoreboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

/**
 * Modernize Edilmiş Skor Tablosu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreboardScreen(
    onBack: () -> Unit,
    viewModel: ScoreboardViewModel = viewModel(factory = ScoreboardViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            AppTopBar(title = "Skor Tablosu", onBack = onBack)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BackgroundImageLayer(
                drawableRes = R.drawable.scoreboard_bg,
                overlayBrush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.44f)
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

            when {
                state.isLoading -> LoadingState()
                state.isEmpty   -> EmptyState()
                else            -> ScoreboardList(state = state)
            }
        }
    }
}

@Composable
private fun ScoreboardList(state: ScoreboardUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. ÖZET İSTATİSTİKLER ---
        item(key = "summary_header") {
            Text(
                text = "Özet İstatistikler",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )
        }
        
        item(key = "summary_grid") {
            StatsGrid(stats = state.stats)
        }

        // --- 2. OYUN GEÇMİŞİ LİSTESİ ---
        item(key = "history_header") {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Geçmiş Oyunlar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        itemsIndexed(
            items = state.records,
            key = { _, record -> record.id }
        ) { index, record ->
            // Staggered giriş animasyonu state'i oluştur
            val visibleState = remember { MutableTransitionState(false) }
            LaunchedEffect(Unit) {
                visibleState.targetState = true
            }

            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(tween(300, delayMillis = (index * 50).coerceAtMost(500))) + 
                        slideInVertically(
                            initialOffsetY = { 100 },
                            animationSpec = spring(stiffness = Spring.StiffnessLow)
                        )
            ) {
                GameRecordCard(
                    record = record,
                    gameNumber = state.records.size - index
                )
            }
        }
    }
}

// --- Modern Özet Grid'i ---------------------------------------------------------

@Composable
private fun StatsGrid(stats: GameStats) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Toplam Oyun",
                value = stats.totalGames.toString(),
                icon = Icons.Filled.PlayArrow,
                colorTint = MaterialTheme.colorScheme.primary
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "En Yüksek Skor",
                value = stats.highScore.toString(),
                icon = Icons.Filled.Star,
                colorTint = MaterialTheme.colorScheme.tertiary
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Ortalama Skor",
                value = stats.avgScore.toString(),
                icon = Icons.Filled.Info,
                colorTint = MaterialTheme.colorScheme.secondary
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Toplam Kelime",
                value = stats.totalWords.toString(),
                icon = Icons.Filled.Favorite,
                colorTint = MaterialTheme.colorScheme.primary
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "En Uzun Kelime",
                value = stats.longestWord?.uppercase(LOCALE_TR) ?: "—",
                icon = Icons.Filled.Star,
                colorTint = MaterialTheme.colorScheme.tertiary
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Toplam Süre",
                value = formatDurationShort(stats.totalDurationSeconds),
                icon = Icons.Filled.DateRange,
                colorTint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    colorTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Renkli ikon kutusu
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colorTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = colorTint
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// --- Tek oyun kartı ----------------------------------------------------

@Composable
private fun GameRecordCard(record: GameRecord, gameNumber: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Başlık bölümü: Oyun No, Tarih ve Skor Puanı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pill şeklinde rozet
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Oyun $gameNumber",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Tarih: ${formatDate(record.playedAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Sağ üst puan
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = record.score.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = if (record.abandoned)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Puan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Detay haplari (chips)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoChip(
                    text = "Grid: ${record.rows}x${record.cols}",
                    bgColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                InfoChip(
                    text = "Kelime Sayısı: ${record.wordCount}",
                    bgColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
                InfoChip(
                    text = "Süre: ${formatDurationShort(record.durationSeconds)}",
                    bgColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Alt satir: Ozel durumlar (En uzun kelime ve Erken Cikis)
            if (record.longestWord.isNotBlank() || record.abandoned) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (record.longestWord.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "En Uzun Kelime:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = record.longestWord.uppercase(LOCALE_TR),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    if (record.abandoned) {
                        Text(
                            text = "Erken Çıkıldı",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.errorContainer,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    text: String,
    bgColor: Color,
    contentColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

// --- Boş / yükleniyor state'leri --------------------------------------

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LoadingView(text = "İstatistikler işleniyor...")
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        EmptyStateView(
            title = "Oyun Geçmişi Boş",
            description = "İlk kelimeni bulduğunda efsanen burada yazılmaya başlayacak!",
            icon = Icons.Filled.Star
        )
    }
}

// --- Yardımcı formatlayıcılar -----------------------------------------

private val LOCALE_TR: Locale = Locale("tr", "TR")

private val DATE_FORMAT: SimpleDateFormat =
    SimpleDateFormat("dd MMM yyyy, HH:mm", LOCALE_TR)

private fun formatDate(epochMs: Long): String =
    DATE_FORMAT.format(Date(epochMs))

private fun formatDurationShort(totalSeconds: Long): String {
    val safe = max(0L, totalSeconds)
    val hours = safe / 3_600
    val minutes = (safe % 3_600) / 60
    val seconds = safe % 60
    return when {
        hours > 0   -> String.format(LOCALE_TR, "%dsa %02ddk %02dsn", hours, minutes, seconds)
        minutes > 0 -> String.format(LOCALE_TR, "%ddk %02dsn", minutes, seconds)
        else        -> String.format(LOCALE_TR, "%dsn", seconds)
    }
}
