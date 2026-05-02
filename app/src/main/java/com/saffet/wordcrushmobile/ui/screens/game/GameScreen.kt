package com.saffet.wordcrushmobile.ui.screens.game

import android.media.AudioManager
import android.media.ToneGenerator
import android.view.SoundEffectConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saffet.wordcrushmobile.R
import com.saffet.wordcrushmobile.ui.components.AppTopBar
import com.saffet.wordcrushmobile.ui.components.BackgroundImageLayer
import com.saffet.wordcrushmobile.ui.components.ConfirmDialog
import com.saffet.wordcrushmobile.ui.components.LoadingView
import com.saffet.wordcrushmobile.ui.components.game.CurrentWordDisplay
import com.saffet.wordcrushmobile.ui.components.game.GameBoard
import com.saffet.wordcrushmobile.ui.components.game.GameStatsBar
import com.saffet.wordcrushmobile.ui.components.game.JokerBar
import com.saffet.wordcrushmobile.ui.components.game.JokerTargetingBanner
import com.saffet.wordcrushmobile.viewmodel.GameViewModel
import kotlinx.coroutines.delay

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
    var feedbackVisual by remember { mutableStateOf<GameFeedbackVisual?>(null) }

    val toneGen = remember { ToneGenerator(AudioManager.STREAM_SYSTEM, 100) }

    DisposableEffect(Unit) {
        onDispose {
            toneGen.release()
        }
    }

    LaunchedEffect(state.lastMessage) {
        val msg = state.lastMessage ?: return@LaunchedEffect

        val isError = msg.contains("geçersiz", ignoreCase = true) ||
            msg.contains("hata", ignoreCase = true) ||
            msg.contains("zaten", ignoreCase = true) ||
            msg.contains("bulunamadı", ignoreCase = true) ||
            msg.contains("yok", ignoreCase = true)

        val feedback = feedbackVisualFromMessage(msg)

        if (isError) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            toneGen.startTone(ToneGenerator.TONE_SUP_ERROR, 200)
            snackbarHostState.showSnackbar(msg)
        } else {
            feedbackVisual = feedback
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            toneGen.startTone(ToneGenerator.TONE_SUP_CONFIRM, 150)
            if (feedback == null) {
                snackbarHostState.showSnackbar(msg)
            }
        }

        viewModel.onDismissMessage()
    }

    LaunchedEffect(state.isGameOver) {
        if (!state.isGameOver || hasNavigatedBack) return@LaunchedEffect
        snackbarHostState.showSnackbar("Oyun bitti! Ana ekrana dönülüyor...")
        delay(AUTO_NAVIGATE_HOME_MS)
        navigateHome()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            AppTopBar(
                title = "Oyun · ${state.rows}×${state.cols}",
                onBack = requestBack
            )

            if (showExitDialog) {
                ConfirmDialog(
                    title = "Oyundan Çık",
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

                val bgColor = if (isError) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
                val contentColor = if (isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BackgroundImageLayer(
                drawableRes = R.drawable.game_screen_bg,
                overlayBrush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background.copy(alpha = 0.16f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.36f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.52f)
                    )
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.scrim.copy(alpha = 0.06f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.scrim.copy(alpha = 0.12f)
                            ),
                            start = Offset.Zero,
                            end = Offset(900f, 1800f)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GameStatsBar(
                    score = state.score,
                    remainingMoves = state.remainingMoves,
                    availableWordCount = state.availableWordCount
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    CurrentWordDisplay(
                        word = state.currentWord,
                        modifier = Modifier.fillMaxWidth()
                    )

                    GameFeedbackOverlay(
                        feedback = feedbackVisual,
                        onFinished = { id ->
                            if (feedbackVisual?.id == id) {
                                feedbackVisual = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                state.jokerTargeting?.let { targeting ->
                    JokerTargetingBanner(
                        state = targeting,
                        onCancel = viewModel::onJokerCancel
                    )
                }

                val dragEnabled = state.jokerTargeting == null && !state.isGameOver
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                    tonalElevation = 1.dp,
                    shadowElevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier.padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isBoardReady) {
                            GameBoard(
                                board = state.board,
                                isSelected = state::isSelected,
                                isLastSelected = state::isLastSelected,
                                onCellClick = viewModel::onCellTapped,
                                modifier = Modifier.fillMaxWidth(),
                                isJokerTarget = state::isJokerTarget,
                                isExploding = state::isExploding,
                                jokerEffect = state.jokerEffect,
                                specialEffects = state.specialEffects,
                                gravityAnimation = state.gravityAnimation,
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
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    tonalElevation = 1.dp,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(
                            start = 12.dp,
                            end = 12.dp,
                            top = 12.dp,
                            bottom = 8.dp
                        )
                    ) {
                        Text(
                            text = "Jokerler",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )

                        JokerBar(
                            inventory = state.jokerInventory,
                            selectedType = state.jokerTargeting?.type,
                            onJokerClick = viewModel::onJokerPressed
                        )
                    }
                }

                AnimatedVisibility(
                    visible = state.isGameOver,
                    enter = fadeIn(tween(GAME_OVER_ENTER_MS)) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = tween(
                            durationMillis = GAME_OVER_ENTER_MS,
                            easing = FastOutSlowInEasing
                        )
                    ) + slideInVertically(
                        initialOffsetY = { 40 },
                        animationSpec = tween(
                            durationMillis = GAME_OVER_ENTER_MS,
                            easing = FastOutSlowInEasing
                        )
                    )
                ) {
                    GameOverCard(
                        score = state.score,
                        wordCount = state.foundWords.size
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun GameFeedbackOverlay(
    feedback: GameFeedbackVisual?,
    onFinished: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(feedback?.id) {
        val current = feedback ?: return@LaunchedEffect
        delay(if (current.isCombo) COMBO_FEEDBACK_OVERLAY_MS else FEEDBACK_OVERLAY_MS)
        onFinished(current.id)
    }

    AnimatedVisibility(
        visible = feedback != null,
        enter = fadeIn(tween(FEEDBACK_ENTER_MS / 2)) +
            scaleIn(
                initialScale = 0.58f,
                animationSpec = tween(
                    durationMillis = FEEDBACK_ENTER_MS,
                    easing = FastOutSlowInEasing
                )
            ) +
            slideInVertically(
                animationSpec = tween(
                    durationMillis = FEEDBACK_ENTER_MS,
                    easing = FastOutSlowInEasing
                ),
                initialOffsetY = { -it }
            ),
        exit = fadeOut(tween(FEEDBACK_EXIT_MS)) +
            scaleOut(
                targetScale = 0.9f,
                animationSpec = tween(
                    durationMillis = FEEDBACK_EXIT_MS,
                    easing = FastOutSlowInEasing
                )
            ) +
            slideOutVertically(
                animationSpec = tween(
                    durationMillis = FEEDBACK_EXIT_MS,
                    easing = FastOutSlowInEasing
                ),
                targetOffsetY = { -it / 3 }
            ),
        modifier = modifier
    ) {
        val shown = feedback ?: return@AnimatedVisibility
        val popScale by animateFloatAsState(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = FEEDBACK_POP_MS,
                easing = FastOutSlowInEasing
            ),
            label = "feedbackPopScale"
        )
        val container = if (shown.isCombo) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
        val content = if (shown.isCombo) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .scale(popScale),
            shape = RoundedCornerShape(26.dp),
            color = container.copy(alpha = 0.94f),
            contentColor = content,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                content.copy(alpha = 0.18f),
                                container.copy(alpha = 0.12f),
                                content.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 13.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                content.copy(alpha = 0.16f),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (shown.isCombo) Icons.Filled.CheckCircle else Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = content
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = shown.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = content
                        )
                        shown.subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = content.copy(alpha = 0.84f)
                            )
                        }
                        shown.detail?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = content.copy(alpha = 0.78f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun feedbackVisualFromMessage(message: String): GameFeedbackVisual? {
    if (message.startsWith("+")) {
        val points = Regex("""\+(\d+)""").find(message)?.groupValues?.getOrNull(1)
        val word = Regex(""""([^"]+)"""").find(message)?.groupValues?.getOrNull(1)
        val comboCount = Regex("""(\d+)[×xX] combo""").find(message)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
        val subWords = Regex("""combo \(([^)]+)\)""")
            .find(message)
            ?.groupValues
            ?.getOrNull(1)
        val isCombo = (comboCount ?: 1) > 1
        return GameFeedbackVisual(
            id = System.nanoTime(),
            title = if (isCombo) "${comboCount}× Combo!" else "Kelime Patladı!",
            subtitle = listOfNotNull(
                points?.let { "+$it puan" },
                word?.let { if (isCombo) "Ana: $it" else it }
            ).joinToString(" · ").ifBlank { null },
            detail = subWords?.let { "Alt: $it" },
            isCombo = isCombo
        )
    }

    if (message.contains("kullanıldı", ignoreCase = true)) {
        return GameFeedbackVisual(
            id = System.nanoTime(),
            title = "Joker Etkisi!",
            subtitle = message,
            detail = null,
            isCombo = false
        )
    }

    if (message.contains("aktif", ignoreCase = true) || message.contains("bırakıldı", ignoreCase = true)) {
        return GameFeedbackVisual(
            id = System.nanoTime(),
            title = "Özel Güç!",
            subtitle = message,
            detail = null,
            isCombo = true
        )
    }

    return null
}

private data class GameFeedbackVisual(
    val id: Long,
    val title: String,
    val subtitle: String?,
    val detail: String?,
    val isCombo: Boolean
)

@Composable
private fun GameOverCard(
    score: Int,
    wordCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
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
private const val FEEDBACK_OVERLAY_MS: Long = 2400L
private const val COMBO_FEEDBACK_OVERLAY_MS: Long = 4200L
private const val FEEDBACK_ENTER_MS: Int = 360
private const val FEEDBACK_EXIT_MS: Int = 260
private const val FEEDBACK_POP_MS: Int = 220
private const val GAME_OVER_ENTER_MS: Int = 520
