package com.saffet.wordcrushmobile.ui.screens.newgame

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saffet.wordcrushmobile.domain.model.GameDifficulty
import com.saffet.wordcrushmobile.ui.components.DifficultyCard
import com.saffet.wordcrushmobile.viewmodel.NewGameViewModel

/**
 * Yeni oyun kurulum ekranı.
 *
 * Kullanıcı üç zorluk seçeneğinden birini seçer; seçim [NewGameViewModel] içinde
 * tutulur. "Başla" butonuna basıldığında [onStartGame] seçili zorluğun tahta
 * boyutu ve hamle sayısı ile tetiklenir — navigation bu parametreleri route'a
 * iletir ([com.saffet.wordcrushmobile.ui.navigation.Screen.Game.createRoute]).
 */
@Composable
fun NewGameScreen(
    onStartGame: (rows: Int, cols: Int, moves: Int) -> Unit,
    onBack: () -> Unit,
    viewModel: NewGameViewModel = viewModel()
) {
    val selected by viewModel.selectedDifficulty.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NewGameHeader()

            Spacer(Modifier.height(24.dp))

            DifficultyList(
                selected = selected,
                onSelect = viewModel::select
            )

            Spacer(Modifier.height(24.dp))

            SelectionSummary(selected = selected)

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    onStartGame(selected.rows, selected.cols, selected.moves)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Başla", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Geri")
            }
        }
    }
}

/**
 * Ekran başlığı ve açıklama metni.
 */
@Composable
private fun NewGameHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Yeni Oyun",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            text = "Bir zorluk seç ve oyuna başla",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Tüm zorluk seçeneklerini dikey olarak listeler.
 * Her kart [DifficultyCard] ile render edilir.
 */
@Composable
private fun DifficultyList(
    selected: GameDifficulty,
    onSelect: (GameDifficulty) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GameDifficulty.entries.forEach { difficulty ->
            DifficultyCard(
                difficulty = difficulty,
                isSelected = difficulty == selected,
                onClick = { onSelect(difficulty) }
            )
        }
    }
}

/**
 * Seçilen zorluğun özetini gösteren küçük bilgi satırı.
 */
@Composable
private fun SelectionSummary(selected: GameDifficulty) {
    Text(
        text = "${selected.label} tahta · ${selected.moves} hamle hakkın var",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary
    )
}
