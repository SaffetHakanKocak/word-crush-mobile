package com.saffet.wordcrushmobile.ui.screens.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.DisposableEffect
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import android.view.SoundEffectConstants
import com.saffet.wordcrushmobile.ui.components.AppTopBar
import com.saffet.wordcrushmobile.ui.components.ConfirmDialog
import com.saffet.wordcrushmobile.ui.components.LoadingView
import com.saffet.wordcrushmobile.ui.components.ScreenContainer
import com.saffet.wordcrushmobile.ui.components.game.CurrentWordDisplay
import com.saffet.wordcrushmobile.ui.components.game.GameBoard
import com.saffet.wordcrushmobile.ui.components.game.GameStatsBar
import com.saffet.wordcrushmobile.ui.components.game.JokerBar
import com.saffet.wordcrushmobile.ui.components.game.JokerTargetingBanner
import com.saffet.wordcrushmobile.viewmodel.GameViewModel
import kotlinx.coroutines.delay

/**
 * Asıl oyun ekranı — modernize edilmiş UI.
 *
 * Yerleşim (yukarıdan aşağı):
 *  1. TopAppBar (geri + yeniden başlat).
 *  2. [GameStatsBar] — skor / kalan hamle / kelime sayısı (ikonlu).
 *  3. [CurrentWordDisplay] — oluşmakta olan kelime (harf chip'leri).
 *  4. [JokerTargetingBanner] — joker hedef seçme modu aktifse.
 *  5. [GameBoard] — harf grid'i.
 *  6. [JokerBar] — alt joker çubuğu (modern kartlar).
 *  7. Oyun bitti kartı — AnimatedVisibility ile fade+scale giriş.
 *
 * Snackbar: Modern rounded tasarım.
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

    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    // Farklı sesler için ToneGenerator
    val toneGen = remember { ToneGenerator(AudioManager.STREAM_SYSTEM, 100) }
    DisposableEffect(Unit) {
        onDispose {
            toneGen.release()
        }
    }

    // Snackbar mesaj gösterimi
    LaunchedEffect(state.lastMessage) {
        val msg = state.lastMessage ?: return@LaunchedEffect
        
        // Mikro etkileşimler: Geri bildirim (Haptic + Sound)
        val isError = msg.contains("geçersiz", ignoreCase = true) || 
                      msg.contains("hata", ignoreCase = true) ||
                      msg.contains("zaten", ignoreCase = true) ||
                      msg.contains("bulunamadı", ignoreCase = true) ||
                      msg.contains("yok", ignoreCase = true)
                      
        if (isError) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            // Error sound: Belirgin hata sesi
            toneGen.startTone(ToneGenerator.TONE_SUP_ERROR, 200)
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            // Success sound: Başarı sesi
            toneGen.startTone(ToneGenerator.TONE_SUP_CONFIRM, 150)
        }
        
        snackbarHostState.showSnackbar(msg)
        viewModel.onDismissMessage()
    }

    // Otomatik ana ekrana dönüş
    LaunchedEffect(state.isGameOver) {
        if (!state.isGameOver || hasNavigatedBack) return@LaunchedEffect
        snackbarHostState.showSnackbar("Oyun bitti! 🎉 Ana ekrana dönülüyor...")
        delay(AUTO_NAVIGATE_HOME_MS)
        navigateHome()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Oyun · ${state.rows}×${state.cols}",
                onBack = requestBack,
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
                ConfirmDialog(
                    title = "Oyundan çık",
                    message = "Çıkmak istediğinize emin misiniz?",
                    confirmText = "Evet",
                    dismissText = "Hayır",
                    onConfirm = {
                        showExitDialog = false
                        navigateHome()
                    },
                    onDismiss = {
                        showExitDialog = false
                    }
                )
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                val msg = data.visuals.message
                val isError = msg.contains("geçersiz", ignoreCase = true) || 
                              msg.contains("hata", ignoreCase = true) ||
                              msg.contains("zaten", ignoreCase = true) ||
                              msg.contains("bulunamadı", ignoreCase = true) ||
                              msg.contains("yok", ignoreCase = true)
                              
                val bgColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                val contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                val icon = if (isError) Icons.Filled.Warning else Icons.Filled.CheckCircle

                Card(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(100.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = bgColor,
                        contentColor = contentColor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = contentColor
                        )
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                }
            }
        }
    ) { padding ->
        ScreenContainer(
            modifier = Modifier.padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Stats bar — üst bilgi alanı
                GameStatsBar(
                    score = state.score,
                    remainingMoves = state.remainingMoves,
                    availableWordCount = state.availableWordCount
                )

                // Kelime oluşturma bandı
                CurrentWordDisplay(word = state.currentWord)

                // Joker hedef seçme modu
                state.jokerTargeting?.let { targeting ->
                    JokerTargetingBanner(
                        state = targeting,
                        onCancel = viewModel::onJokerCancel
                    )
                }

                // Oyun tahtası
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
                    LoadingView(
                        text = "Tahta hazırlanıyor...",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Joker barı
                JokerBar(
                    inventory = state.jokerInventory,
                    selectedType = state.jokerTargeting?.type,
                    onJokerClick = viewModel::onJokerPressed
                )

                // Oyun bitti kartı — animasyonlu giriş
                AnimatedVisibility(
                    visible = state.isGameOver,
                    enter = fadeIn(tween(400)) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = tween(400)
                    ) + slideInVertically(
                        initialOffsetY = { 40 },
                        animationSpec = tween(400)
                    )
                ) {
                    GameOverCard(
                        score = state.score,
                        wordCount = state.foundWords.size
                    )
                }
            }
        }
    }
}

/**
 * Oyun sonu özet kartı — gradient arka plan, skor ve kelime sayısı.
 */
@Composable
private fun GameOverCard(
    score: Int,
    wordCount: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            )
            .padding(vertical = 20.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Oyun Bitti!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = "Skor: $score · $wordCount kelime buldun",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private const val AUTO_NAVIGATE_HOME_MS: Long = 1200L
