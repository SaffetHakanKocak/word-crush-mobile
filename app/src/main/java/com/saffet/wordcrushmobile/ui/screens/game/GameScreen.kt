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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saffet.wordcrushmobile.ui.components.game.CurrentWordDisplay
import com.saffet.wordcrushmobile.ui.components.game.GameBoard
import com.saffet.wordcrushmobile.ui.components.game.GameStatsBar
import com.saffet.wordcrushmobile.ui.components.game.JokerBar
import com.saffet.wordcrushmobile.ui.components.game.JokerTargetingBanner
import com.saffet.wordcrushmobile.viewmodel.GameViewModel
import kotlinx.coroutines.delay

/**
 * Asıl oyun ekranı — ilk çalışan sürüm.
 *
 * Yerleşim (yukarıdan aşağı):
 *  1. TopAppBar (geri + yeniden başlat).
 *  2. [GameStatsBar] — skor / kalan hamle / kelime sayısı.
 *  3. [CurrentWordDisplay] — o an oluşmakta olan kelime.
 *  4. [GameBoard] — harf grid'i. PDF §Oyun Akışı'na uygun **drag tabanlı
 *     seçim** varsayılandır: parmağı ilk harfe bastır, komşu harflerin
 *     üzerinde sürükle, kaldırınca kelime otomatik finalize olur. Joker
 *     targeting modunda drag pasif olur, tap ile hedef seçilir.
 *  5. [JokerBar] — alt joker çubuğu.
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
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    var hasNavigatedBack by rememberSaveable { mutableStateOf(false) }

    // Ekrandan çıkış tek noktadan yönetilir; böylece hem otomatik dönüş hem
    // kullanıcı onayı ile çıkış aynı kaydetme/navigate akışını kullanır.
    val navigateHome: () -> Unit = navigate@{
        if (hasNavigatedBack) return@navigate
        hasNavigatedBack = true
        viewModel.onExitGame()
        onBack()
    }

    val requestBack: () -> Unit = {
        if (state.isGameOver) {
            navigateHome()
        } else {
            showExitDialog = true
        }
    }
    BackHandler(onBack = requestBack)

    // Geçici mesajları snackbar ile göster; mesaj kaybolduğunda VM'e bildir.
    LaunchedEffect(state.lastMessage) {
        val msg = state.lastMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.onDismissMessage()
    }

    // Oyun bittiğinde kısa bilgilendirme verip otomatik ana ekrana dön.
    LaunchedEffect(state.isGameOver) {
        if (!state.isGameOver || hasNavigatedBack) return@LaunchedEffect
        snackbarHostState.showSnackbar("Oyun bitti. Ana ekrana dönülüyor...")
        delay(AUTO_NAVIGATE_HOME_MS)
        navigateHome()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oyun · ${state.rows}x${state.cols}") },
                navigationIcon = {
                    IconButton(onClick = requestBack) {
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

            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text("Oyundan çık") },
                    text = { Text("Çıkmak istediğinize emin misiniz?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showExitDialog = false
                                navigateHome()
                            }
                        ) {
                            Text("Evet")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showExitDialog = false }) {
                            Text("Hayır")
                        }
                    }
                )
            }
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

                // Drag yalnızca oyuncu kelime kurarken aktif. Joker targeting
                // modunda tap ile hedef seçilir — drag layer'ı kapanır.
                val dragEnabled = state.jokerTargeting == null && !state.isGameOver
                if (state.isBoardReady) {
                    GameBoard(
                        board = state.board,
                        isSelected = state::isSelected,
                        isLastSelected = state::isLastSelected,
                        onCellClick = viewModel::onCellTapped,
                        modifier = Modifier.fillMaxWidth(),
                        isJokerTarget = state::isJokerTarget,
                        isExploding = state::isExploding,
                        enableDrag = dragEnabled,
                        onDragStartCell = viewModel::onDragStartCell,
                        onDragOverCell = viewModel::onDragOverCell,
                        onDragEnd = viewModel::onDragEnd,
                        onDragCancel = viewModel::onDragCancel
                    )
                } else {
                    Text(
                        text = "Tahta hazırlanıyor...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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

private const val AUTO_NAVIGATE_HOME_MS: Long = 1200L
