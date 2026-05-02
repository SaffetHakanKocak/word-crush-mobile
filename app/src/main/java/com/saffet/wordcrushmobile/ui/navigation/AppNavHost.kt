package com.saffet.wordcrushmobile.ui.navigation

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.saffet.wordcrushmobile.R
import com.saffet.wordcrushmobile.ui.screens.game.GameScreen
import com.saffet.wordcrushmobile.ui.screens.home.HomeScreen
import com.saffet.wordcrushmobile.ui.screens.market.MarketScreen
import com.saffet.wordcrushmobile.ui.screens.newgame.MoveCountSelectionScreen
import com.saffet.wordcrushmobile.ui.screens.newgame.NewGameScreen
import com.saffet.wordcrushmobile.ui.screens.scoreboard.ScoreboardScreen
import com.saffet.wordcrushmobile.ui.screens.splash.SplashScreen
import com.saffet.wordcrushmobile.ui.screens.username.UsernameScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    val context = LocalContext.current
    var isMusicEnabled by rememberSaveable { mutableStateOf(true) }
    var musicStatus by remember { mutableStateOf(MusicStatus.Loading) }
    val backgroundMusicState = remember(context) {
        mutableStateOf(createBackgroundMusicPlayer(context))
    }
    val backgroundMusic = backgroundMusicState.value

    DisposableEffect(backgroundMusic) {
        if (backgroundMusic == null) {
            musicStatus = MusicStatus.Error
            onDispose { }
        } else {
            backgroundMusic.setOnErrorListener { player, what, extra ->
                Log.w("AppNavHost", "Background music error: what=$what extra=$extra")
                player.runCatching { reset() }
                player.release()
                backgroundMusicState.value = createBackgroundMusicPlayer(context)
                musicStatus = if (backgroundMusicState.value == null) MusicStatus.Error else MusicStatus.Ready
                true
            }
            backgroundMusic.setOnCompletionListener { player ->
                if (isMusicEnabled && !player.isLooping) {
                    player.start()
                    musicStatus = MusicStatus.Playing
                }
            }
            musicStatus = if (backgroundMusic.isPlaying) MusicStatus.Playing else MusicStatus.Ready

            onDispose {
                backgroundMusic.runCatching { stop() }
                backgroundMusic.release()
            }
        }
    }

    LaunchedEffect(backgroundMusic, isMusicEnabled) {
        val player = backgroundMusic
        if (player == null) {
            musicStatus = MusicStatus.Error
            return@LaunchedEffect
        }

        if (isMusicEnabled) {
            runCatching {
                if (!player.isPlaying) {
                    player.start()
                }
            }.onSuccess {
                musicStatus = MusicStatus.Playing
            }.onFailure {
                Log.w("AppNavHost", "Background music start failed", it)
                musicStatus = MusicStatus.Error
            }
        } else {
            runCatching {
                if (player.isPlaying) {
                    player.pause()
                }
            }
            musicStatus = MusicStatus.Ready
        }
    }

    Box {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Username.route) {
                UsernameScreen(
                    onSaved = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Username.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNewGame = { navController.navigate(Screen.NewGame.route) },
                    onScoreboard = { navController.navigate(Screen.Scoreboard.route) },
                    onMarket = { navController.navigate(Screen.Market.route) }
                )
            }

            composable(Screen.NewGame.route) {
                NewGameScreen(
                    onNext = { rows, cols ->
                        navController.navigate(Screen.MoveSelection.createRoute(rows, cols))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.MoveSelection.route,
                arguments = listOf(
                    navArgument(Screen.MoveSelection.ARG_ROWS) { type = NavType.IntType },
                    navArgument(Screen.MoveSelection.ARG_COLS) { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val rows = backStackEntry.arguments?.getInt(Screen.MoveSelection.ARG_ROWS) ?: 8
                val cols = backStackEntry.arguments?.getInt(Screen.MoveSelection.ARG_COLS) ?: 8

                MoveCountSelectionScreen(
                    rows = rows,
                    cols = cols,
                    onStartGame = { r, c, moves ->
                        navController.navigate(Screen.Game.createRoute(r, c, moves))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Game.route,
                arguments = listOf(
                    navArgument(Screen.Game.ARG_ROWS) { type = NavType.IntType },
                    navArgument(Screen.Game.ARG_COLS) { type = NavType.IntType },
                    navArgument(Screen.Game.ARG_MOVES) { type = NavType.IntType }
                )
            ) {
                GameScreen(
                    onBack = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Scoreboard.route) {
                ScoreboardScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Market.route) {
                MarketScreen(onBack = { navController.popBackStack() })
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 0.dp, end = 16.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(chipBackgroundColor(isMusicEnabled = isMusicEnabled, musicStatus = musicStatus))
                .clickable {
                    if (musicStatus == MusicStatus.Error) {
                        backgroundMusicState.value?.release()
                        backgroundMusicState.value = createBackgroundMusicPlayer(context)
                        musicStatus = if (backgroundMusicState.value == null) MusicStatus.Error else MusicStatus.Ready
                        if (backgroundMusicState.value != null) {
                            isMusicEnabled = true
                        }
                    } else {
                        isMusicEnabled = !isMusicEnabled
                    }
                }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = when (musicStatus) {
                    MusicStatus.Error -> "!"
                    MusicStatus.Loading -> "..."
                    MusicStatus.Ready -> "♪"
                    MusicStatus.Playing -> "♪"
                },
                style = MaterialTheme.typography.titleMedium,
                color = chipContentColor(isMusicEnabled = isMusicEnabled, musicStatus = musicStatus)
            )
            Text(
                text = when (musicStatus) {
                    MusicStatus.Error -> "Ses Hatası"
                    MusicStatus.Loading -> "Ses Yükleniyor"
                    MusicStatus.Playing -> "Ses Açık"
                    MusicStatus.Ready -> if (isMusicEnabled) "Ses Açık" else "Ses Kapalı"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = chipContentColor(isMusicEnabled = isMusicEnabled, musicStatus = musicStatus)
            )
        }
    }
}

@Composable
private fun chipBackgroundColor(
    isMusicEnabled: Boolean,
    musicStatus: MusicStatus
) = when {
    musicStatus == MusicStatus.Error -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.94f)
    isMusicEnabled -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f)
}

@Composable
private fun chipContentColor(
    isMusicEnabled: Boolean,
    musicStatus: MusicStatus
) = when {
    musicStatus == MusicStatus.Error -> MaterialTheme.colorScheme.onErrorContainer
    isMusicEnabled -> MaterialTheme.colorScheme.onPrimaryContainer
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun createBackgroundMusicPlayer(context: Context): MediaPlayer? {
    val candidates = listOf(R.raw.bgm_1184, R.raw.game_bgm)
    for (resId in candidates) {
        val player = runCatching { MediaPlayer.create(context, resId) }.getOrNull() ?: continue
        return player.apply {
            isLooping = true
            setVolume(NAV_BGM_VOLUME, NAV_BGM_VOLUME)
        }
    }
    return null
}

private enum class MusicStatus {
    Loading,
    Ready,
    Playing,
    Error
}

private const val NAV_BGM_VOLUME: Float = 0.32f
