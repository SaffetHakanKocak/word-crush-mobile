package com.saffet.wordcrushmobile.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saffet.wordcrushmobile.R
import com.saffet.wordcrushmobile.ui.components.BackgroundImageLayer
import com.saffet.wordcrushmobile.ui.theme.Elevations
import com.saffet.wordcrushmobile.ui.theme.WordCrushTheme
import com.saffet.wordcrushmobile.viewmodel.SplashViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigate: (route: String) -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val nextRoute by viewModel.nextRoute.collectAsStateWithLifecycle()
    var showIntro by remember { mutableStateOf(false) }

    val pulse = rememberInfiniteTransition(label = "splashPulse")
    val glowScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )

    LaunchedEffect(Unit) {
        showIntro = true
    }

    LaunchedEffect(nextRoute) {
        val route = nextRoute ?: return@LaunchedEffect
        delay(SPLASH_NAVIGATION_DELAY_MS)
        onNavigate(route)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundImageLayer(
            drawableRes = R.drawable.splash_background,
            overlayBrush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.background.copy(alpha = 0.08f),
                    MaterialTheme.colorScheme.background.copy(alpha = 0.16f),
                    MaterialTheme.colorScheme.background.copy(alpha = 0.26f)
                )
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.04f),
                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.08f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = showIntro,
                enter = fadeIn(animationSpec = tween(700)) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(700, easing = FastOutSlowInEasing)
                    )
            ) {
                ElevatedCard(
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = Elevations.medium
                    )
                ) {
                    Column(
                        modifier = Modifier.graphicsLayer {
                            scaleX = glowScale
                            scaleY = glowScale
                        },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(WordCrushTheme.spacing.sm)
                    ) {
                        Spacer(Modifier.height(WordCrushTheme.spacing.sm))
                        Image(
                            painter = painterResource(id = R.drawable.word_crush),
                            contentDescription = "Word Crush Logo",
                            modifier = Modifier.size(160.dp)
                        )
                        Text(
                            text = "Hazırlanıyor...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(WordCrushTheme.spacing.xs))
                        LinearProgressIndicator(
                            modifier = Modifier.size(width = 180.dp, height = 6.dp),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(Modifier.height(WordCrushTheme.spacing.sm))
                    }
                }
            }

            Spacer(Modifier.height(WordCrushTheme.spacing.lg))
            Row(
                horizontalArrangement = Arrangement.spacedBy(WordCrushTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Sözlük ve profil yükleniyor",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private const val SPLASH_NAVIGATION_DELAY_MS: Long = 850L
