package com.saffet.wordcrushmobile.ui.screens.scoreboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saffet.wordcrushmobile.domain.model.GameRecord
import com.saffet.wordcrushmobile.domain.model.GameStats
import com.saffet.wordcrushmobile.viewmodel.ScoreboardUiState
import com.saffet.wordcrushmobile.viewmodel.ScoreboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

/**
 * Skor Tablosu ekranı.
 *
 * Yerleşim:
 *  - TopAppBar (geri).
 *  - Liste (LazyColumn):
 *      - item: [StatsSummaryCard] — özet istatistikler.
 *      - items: her bir [GameRecordCard] — en yeni oyun en üstte.
 *  - Boş-state: kayıt yoksa bilgilendirici mesaj + ikon.
 *
 * Tüm veri Room'dan reaktif geldiği için ekran kendi başına refresh mantığı
 * içermez — yeni oyun kaydedildiğinde liste otomatik güncellenir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreboardScreen(
    onBack: () -> Unit,
    viewModel: ScoreboardViewModel = viewModel(factory = ScoreboardViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ScoreboardContent(state = state, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScoreboardContent(
    state: ScoreboardUiState,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skor Tablosu") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "summary") {
            StatsSummaryCard(stats = state.stats)
        }
        item(key = "history-header") {
            Text(
                text = "Oyun Geçmişi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
        items(items = state.records, key = { it.id }) { record ->
            GameRecordCard(record = record)
        }
    }
}

// --- Özet kart ---------------------------------------------------------

@Composable
private fun StatsSummaryCard(stats: GameStats) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "Özet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            StatRow(label = "Toplam oyun", value = stats.totalGames.toString())
            StatRow(label = "En yüksek puan", value = stats.highScore.toString())
            StatRow(label = "Ortalama puan", value = stats.avgScore.toString())
            StatRow(label = "Toplam kelime", value = stats.totalWords.toString())
            StatRow(
                label = "En uzun kelime",
                value = stats.longestWord?.uppercase(LOCALE_TR) ?: "—"
            )
            StatRow(
                label = "Toplam süre",
                value = formatDuration(stats.totalDurationSeconds)
            )
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// --- Tek oyun kartı ----------------------------------------------------

@Composable
private fun GameRecordCard(record: GameRecord) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(record.playedAt),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${record.score} puan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (record.abandoned)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = buildString {
                    append("${record.rows}x${record.cols} · ")
                    append("${record.wordCount} kelime · ")
                    append("${record.movesUsed}/${record.totalMoves} hamle · ")
                    append(formatDuration(record.durationSeconds))
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (record.longestWord.isNotBlank()) {
                Text(
                    text = "En uzun: ${record.longestWord.uppercase(LOCALE_TR)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (record.abandoned) {
                Text(
                    text = "Erken çıkıldı",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// --- Boş / yükleniyor state'leri --------------------------------------

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(72.dp)
            )
            Text(
                text = "Henüz kayıt yok",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "İlk oyununu oyna; skorun burada görünecek.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
        }
    }
}

// --- Yardımcı formatlayıcılar -----------------------------------------

private val LOCALE_TR: Locale = Locale("tr", "TR")

/** Oyun kaydı başlıkları için kullanılan "gg.aa.yyyy HH:mm" biçimi. */
private val DATE_FORMAT: SimpleDateFormat =
    SimpleDateFormat("dd.MM.yyyy HH:mm", LOCALE_TR)

private fun formatDate(epochMs: Long): String =
    DATE_FORMAT.format(Date(epochMs))

/**
 * Saniye cinsinden süreyi "1s 03d 12sn", "03d 05sn" veya "05sn" biçimine çevirir.
 * Sıfır veya negatif değerlerde "0sn" döner.
 */
private fun formatDuration(totalSeconds: Long): String {
    val safe = max(0L, totalSeconds)
    val hours = safe / 3_600
    val minutes = (safe % 3_600) / 60
    val seconds = safe % 60
    return when {
        hours > 0   -> String.format(LOCALE_TR, "%ds %02dd %02dsn", hours, minutes, seconds)
        minutes > 0 -> String.format(LOCALE_TR, "%dd %02dsn", minutes, seconds)
        else        -> String.format(LOCALE_TR, "%dsn", seconds)
    }
}
