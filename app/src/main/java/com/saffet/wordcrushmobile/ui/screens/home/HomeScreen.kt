package com.saffet.wordcrushmobile.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saffet.wordcrushmobile.R
import com.saffet.wordcrushmobile.ui.components.ChangeUsernameDialog
import com.saffet.wordcrushmobile.ui.components.HomeMenuButton
import com.saffet.wordcrushmobile.ui.components.UserGreetingHeader
import com.saffet.wordcrushmobile.viewmodel.HomeViewModel
import kotlinx.coroutines.delay

/**
 * Ana menu ekrani.
 *
 * Kullanici adini sol ustte tiklanabilir olarak gosterir, logoyu orta bolgede
 * vurgular ve mevcut ana menu kartlarini korur.
 */
@Composable
fun HomeScreen(
    onNewGame: () -> Unit,
    onScoreboard: () -> Unit,
    onMarket: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val username by viewModel.username.collectAsStateWithLifecycle()
    var showUsernameDialog by remember { mutableStateOf(false) }

    var headerVisible by remember { mutableStateOf(false) }
    var logoVisible by remember { mutableStateOf(false) }
    var sectionTitleVisible by remember { mutableStateOf(false) }
    var menuItem1Visible by remember { mutableStateOf(false) }
    var menuItem2Visible by remember { mutableStateOf(false) }
    var menuItem3Visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        headerVisible = true
        delay(120)
        logoVisible = true
        delay(120)
        sectionTitleVisible = true
        delay(100)
        menuItem1Visible = true
        delay(100)
        menuItem2Visible = true
        delay(100)
        menuItem3Visible = true
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        initialOffsetY = { -40 },
                        animationSpec = tween(500)
                    ),
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    UserGreetingHeader(
                        username = username,
                        onEditClick = { showUsernameDialog = true }
                    )
                }

                Spacer(Modifier.height(40.dp))

                AnimatedVisibility(
                    visible = logoVisible,
                    enter = fadeIn(tween(600)) + slideInVertically(
                        initialOffsetY = { -20 },
                        animationSpec = tween(600)
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Column(
                        modifier = Modifier.widthIn(max = 280.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.word_crush),
                            contentDescription = "Word Crush Logo",
                            modifier = Modifier.size(188.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = sectionTitleVisible,
                    enter = fadeIn(tween(400)) + slideInVertically(
                        initialOffsetY = { 20 },
                        animationSpec = tween(400)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ne yapmak istersin?",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                HomeMenuContent(
                    onNewGame = onNewGame,
                    onScoreboard = onScoreboard,
                    onMarket = onMarket,
                    menuItem1Visible = menuItem1Visible,
                    menuItem2Visible = menuItem2Visible,
                    menuItem3Visible = menuItem3Visible
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showUsernameDialog) {
        ChangeUsernameDialog(
            initialValue = username,
            onConfirm = { newName ->
                viewModel.saveUsername(newName)
                showUsernameDialog = false
            },
            onDismiss = { showUsernameDialog = false }
        )
    }
}

@Composable
private fun HomeMenuContent(
    onNewGame: () -> Unit,
    onScoreboard: () -> Unit,
    onMarket: () -> Unit,
    menuItem1Visible: Boolean,
    menuItem2Visible: Boolean,
    menuItem3Visible: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AnimatedVisibility(
            visible = menuItem1Visible,
            enter = fadeIn(tween(400)) + slideInVertically(
                initialOffsetY = { 60 },
                animationSpec = tween(400)
            )
        ) {
            HomeMenuButton(
                label = "Yeni Oyun",
                description = "Kelime bulmacasına başla",
                icon = Icons.Filled.PlayArrow,
                onClick = onNewGame,
                iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                iconTintColor = MaterialTheme.colorScheme.primary
            )
        }

        AnimatedVisibility(
            visible = menuItem2Visible,
            enter = fadeIn(tween(400)) + slideInVertically(
                initialOffsetY = { 60 },
                animationSpec = tween(400)
            )
        ) {
            HomeMenuButton(
                label = "Skor Tablosu",
                description = "En yüksek skorlarını gör",
                icon = Icons.Filled.Star,
                onClick = onScoreboard,
                iconBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                iconTintColor = MaterialTheme.colorScheme.tertiary
            )
        }

        AnimatedVisibility(
            visible = menuItem3Visible,
            enter = fadeIn(tween(400)) + slideInVertically(
                initialOffsetY = { 60 },
                animationSpec = tween(400)
            )
        ) {
            HomeMenuButton(
                label = "Market",
                description = "Joker ve güçlendirmeler al",
                icon = Icons.Filled.ShoppingCart,
                onClick = onMarket,
                iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                iconTintColor = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
