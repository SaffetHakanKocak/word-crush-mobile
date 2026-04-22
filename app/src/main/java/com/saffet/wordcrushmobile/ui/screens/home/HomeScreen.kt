package com.saffet.wordcrushmobile.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saffet.wordcrushmobile.ui.components.ChangeUsernameDialog
import com.saffet.wordcrushmobile.ui.components.HomeMenuButton
import com.saffet.wordcrushmobile.ui.components.UserGreetingHeader
import com.saffet.wordcrushmobile.ui.components.UsernameChip
import com.saffet.wordcrushmobile.viewmodel.HomeViewModel

/**
 * Ana menü ekranı.
 *
 * Ekran şu bölümlerden oluşur:
 *  1. Sol üstte tıklanabilir [UsernameChip] — tıklanınca
 *     [ChangeUsernameDialog] açılır (PDF §"Ana ekranın sol üst kısmında yer alan
 *     kullanıcı isminin üzerine tıklayarak değiştirilecektir").
 *  2. [UserGreetingHeader] — DataStore'dan gelen kullanıcı adını hoş geldin
 *     kartı olarak gösterir.
 *  3. [HomeMenuContent] — "Yeni Oyun", "Skor Tablosu" ve "Market" girişleri.
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Sol üst köşe: tıklanabilir kullanıcı adı.
            // Username henüz yüklenmediyse chip'i hiç render etmeyiz, böylece
            // "…" gibi bir placeholder üzerine yanlışlıkla tıklanmaz.
            if (username.isNotBlank()) {
                UsernameChip(
                    username = username,
                    onClick = { showUsernameDialog = true },
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UserGreetingHeader(username = username)

                Spacer(Modifier.height(40.dp))

                HomeMenuContent(
                    onNewGame = onNewGame,
                    onScoreboard = onScoreboard,
                    onMarket = onMarket
                )
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

/**
 * Ana menü butonlarını dikey olarak dizen özel bileşen.
 * Her buton [HomeMenuButton] kullanır; sıralama ve boşluklar burada yönetilir.
 */
@Composable
private fun HomeMenuContent(
    onNewGame: () -> Unit,
    onScoreboard: () -> Unit,
    onMarket: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HomeMenuButton(
            label = "Yeni Oyun",
            icon = Icons.Filled.PlayArrow,
            onClick = onNewGame
        )
        HomeMenuButton(
            label = "Skor Tablosu",
            icon = Icons.Filled.Star,
            onClick = onScoreboard
        )
        HomeMenuButton(
            label = "Market",
            icon = Icons.Filled.ShoppingCart,
            onClick = onMarket
        )
    }
}
