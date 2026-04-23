package com.saffet.wordcrushmobile.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saffet.wordcrushmobile.ui.components.ChangeUsernameDialog
import com.saffet.wordcrushmobile.ui.components.HomeMenuButton
import com.saffet.wordcrushmobile.ui.components.UserGreetingHeader
import com.saffet.wordcrushmobile.viewmodel.HomeViewModel
import kotlinx.coroutines.delay

/**
 * Ana menü ekranı.
 *
 * Ekran şu bölümlerden oluşur:
 *  1. Gradient arka planlı [UserGreetingHeader] — kullanıcı adını gösterir,
 *     tıklanınca [ChangeUsernameDialog] açılır
 *     (PDF §"Ana ekranın sol üst kısmında yer alan kullanıcı isminin
 *     üzerine tıklayarak değiştirilecektir").
 *  2. [HomeMenuContent] — "Yeni Oyun", "Skor Tablosu" ve "Market" girişleri
 *     kart benzeri modern tasarımla.
 *
 * Giriş animasyonları: elemanlar staggered fade+slide ile görünür.
 *
 * Ayrı bir "Adı Değiştir" butonu YOKTUR; ilk kayıt akışı hâlâ Splash →
 * UsernameScreen üzerinden gider, buradaki dialog yalnızca değiştirme için.
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

    // Staggered animation states
    var headerVisible by remember { mutableStateOf(false) }
    var sectionTitleVisible by remember { mutableStateOf(false) }
    var menuItem1Visible by remember { mutableStateOf(false) }
    var menuItem2Visible by remember { mutableStateOf(false) }
    var menuItem3Visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        headerVisible = true
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Greeting header — tıklanabilir, isim değiştirir
            AnimatedVisibility(
                visible = headerVisible,
                enter = fadeIn(tween(500)) + slideInVertically(
                    initialOffsetY = { -40 },
                    animationSpec = tween(500)
                )
            ) {
                UserGreetingHeader(
                    username = username,
                    onEditClick = { showUsernameDialog = true }
                )
            }

            Spacer(Modifier.height(32.dp))

            // "Menü" bölüm başlığı
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

            // Menü kartları — staggered geliş
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

/**
 * Ana menü butonlarını dikey olarak dizen özel bileşen.
 * Her buton [HomeMenuButton] kullanır; sıralama ve boşluklar burada yönetilir.
 * Staggered animasyon visibility state'leri dışarıdan alınır.
 */
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
