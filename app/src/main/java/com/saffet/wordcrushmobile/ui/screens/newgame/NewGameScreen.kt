package com.saffet.wordcrushmobile.ui.screens.newgame

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saffet.wordcrushmobile.domain.model.GridSize
import com.saffet.wordcrushmobile.ui.components.DecorativeImageScreenContainer
import com.saffet.wordcrushmobile.ui.components.GridSizeCard
import com.saffet.wordcrushmobile.viewmodel.NewGameViewModel
import kotlinx.coroutines.delay

@Composable
fun NewGameScreen(
    onNext: (rows: Int, cols: Int) -> Unit,
    onBack: () -> Unit,
    viewModel: NewGameViewModel = viewModel()
) {
    val selected by viewModel.selectedGridSize.collectAsStateWithLifecycle()

    var headerVisible by remember { mutableStateOf(false) }
    var cardsVisible by remember { mutableStateOf(false) }
    var summaryVisible by remember { mutableStateOf(false) }
    var buttonsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        headerVisible = true
        delay(150)
        cardsVisible = true
        delay(200)
        summaryVisible = true
        delay(100)
        buttonsVisible = true
    }

    DecorativeImageScreenContainer {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = headerVisible,
                enter = fadeIn(tween(500)) + slideInVertically(
                    initialOffsetY = { -40 },
                    animationSpec = tween(500)
                )
            ) {
                NewGameHeader(onBack = onBack)
            }

            Spacer(Modifier.height(28.dp))

            AnimatedVisibility(
                visible = cardsVisible,
                enter = fadeIn(tween(500)) + slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(500)
                )
            ) {
                GridSizeList(
                    selected = selected,
                    onSelect = viewModel::select
                )
            }

            Spacer(Modifier.height(20.dp))

            AnimatedVisibility(
                visible = summaryVisible,
                enter = fadeIn(tween(400)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = tween(400)
                )
            ) {
                SelectionSummary(selected = selected)
            }

            Spacer(Modifier.height(28.dp))

            AnimatedVisibility(
                visible = buttonsVisible,
                enter = fadeIn(tween(400)) + slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = tween(400)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NextButton(
                        onClick = {
                            onNext(selected.rows, selected.cols)
                        }
                    )

                    androidx.compose.material3.TextButton(onClick = onBack) {
                        Text(
                            text = "Vazgeç",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewGameHeader(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Yeni Oyun",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Oyun tahtasının boyutunu seç.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun GridSizeList(
    selected: GridSize,
    onSelect: (GridSize) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300)),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GridSize.entries.forEach { gridSize ->
            GridSizeCard(
                gridSize = gridSize,
                isSelected = gridSize == selected,
                onClick = { onSelect(gridSize) }
            )
        }
    }
}

@Composable
private fun SelectionSummary(selected: GridSize) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .animateContentSize(animationSpec = tween(300))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryItem(
                value = "${selected.rows}×${selected.cols}",
                label = "Tahta Boyutu"
            )
        }
    }
}

@Composable
private fun SummaryItem(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "nextBtnScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.primary)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "İleri",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
