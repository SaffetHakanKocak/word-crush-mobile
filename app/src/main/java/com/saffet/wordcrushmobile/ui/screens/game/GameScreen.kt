package com.saffet.wordcrushmobile.ui.screens.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saffet.wordcrushmobile.ui.components.game.CurrentWordDisplay
import com.saffet.wordcrushmobile.ui.components.game.GameActionButtons
import com.saffet.wordcrushmobile.ui.components.game.GameBoard
import com.saffet.wordcrushmobile.ui.components.game.GameStatsBar
import com.saffet.wordcrushmobile.ui.components.game.JokerBar
import com.saffet.wordcrushmobile.ui.components.game.JokerTargetingBanner
import com.saffet.wordcrushmobile.viewmodel.GameViewModel

/**
 * Asıl oyun ekranı — ilk çalışan sürüm.
 *
 * Yerleşim (yukarıdan aşağı):
 *  1. TopAppBar (geri + yeniden başlat).
 *  2. [GameStatsBar] — skor / kalan hamle / kelime sayısı.
 *  3. [CurrentWordDisplay] — o an oluşmakta olan kelime.
 *  4. [GameBoard] — harf grid'i (tıklanabilir hücreler).
 *  5. [GameActionButtons] — Temizle / Onayla.
 *
 * Tüm state [GameViewModel] içinde tutulur; ekran sadece gözlem ve olay
 * iletim sorumluluğunu üstlenir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    onBack: () -> Unit,
    viewModel: GameViewModel = viewModel(factory = GameViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Kullanıcı oyun ekranından her nasıl çıkarsa çıksın (TopAppBar ok veya
    // sistem geri tuşu), önce ViewModel'e bildir — oturum erken çıkış
    // olarak kaydedilsin. Oyun normal bittiyse çifte kayıt olmaz
    // (persistIfNeeded idempotenttir).
    val handleBack: () -> Unit = {
        viewModel.onExitGame()
        onBack()
    }
    BackHandler(onBack = handleBack)

    // Geçici mesajları snackbar ile göster; mesaj kaybolduğunda VM'e bildir.
    LaunchedEffect(state.lastMessage) {
        val msg = state.lastMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.onDismissMessage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oyun · ${state.rows}x${state.cols}") },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onRestart) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Yeniden Başlat"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GameStatsBar(
                    score = state.score,
                    remainingMoves = state.remainingMoves,
                    availableWordCount = state.availableWordCount
                )

                CurrentWordDisplay(word = state.currentWord)

                // Joker hedef seçme modunda prompt + iptal. Mod kapalıyken
                // banner tamamen render edilmez; layout boşluğu oluşmaz.
                state.jokerTargeting?.let { targeting ->
                    JokerTargetingBanner(
                        state = targeting,
                        onCancel = viewModel::onJokerCancel
                    )
                }

                GameBoard(
                    board = state.board,
                    isSelected = state::isSelected,
                    isLastSelected = state::isLastSelected,
                    onCellClick = viewModel::onCellTapped,
                    modifier = Modifier.fillMaxWidth(),
                    isJokerTarget = state::isJokerTarget
                )

                GameActionButtons(
                    canSubmit = state.selectedCells.isNotEmpty() &&
                        state.isDictionaryReady &&
                        !state.isGameOver,
                    canClear = state.selectedCells.isNotEmpty(),
                    onClear = viewModel::onClearSelection,
                    onSubmit = viewModel::onSubmitWord
                )

                // Alt joker barı — market envanterine bağlı, targeting state
                // ile hangi jokerin seçili olduğu gösterilir. Oyun bittiyse
                // buton tıklamaları ViewModel'de zaten sessizce ignore edilir.
                JokerBar(
                    inventory = state.jokerInventory,
                    selectedType = state.jokerTargeting?.type,
                    onJokerClick = viewModel::onJokerPressed
                )

                if (state.isGameOver) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Oyun bitti · Skor: ${state.score} · Kelime: ${state.foundWords.size}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
