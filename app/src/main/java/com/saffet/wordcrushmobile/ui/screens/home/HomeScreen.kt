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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saffet.wordcrushmobile.ui.components.HomeMenuButton
import com.saffet.wordcrushmobile.ui.components.UserGreetingHeader
import com.saffet.wordcrushmobile.viewmodel.HomeViewModel

/**
 * Ana menü ekranı.
 *
 * Ekran üç mantıksal bölümden oluşur:
 *  1. [UserGreetingHeader] — DataStore'dan gelen kullanıcı adını gösterir.
 *  2. [HomeMenuContent] — "Yeni Oyun", "Skor Tablosu" ve "Market" girişleri.
 *  3. Alt kısımdaki "Adı Değiştir" TextButton.
 *
 * HomeScreen sadece compose & orkestrasyon yapar; görsel bileşenler
 * [com.saffet.wordcrushmobile.ui.components] paketi altındadır ve
 * bağımsız olarak önizlenip test edilebilir.
 */
@Composable
fun HomeScreen(
    onNewGame: () -> Unit,
    onScoreboard: () -> Unit,
    onMarket: () -> Unit,
    onChangeUsername: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val username by viewModel.username.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserGreetingHeader(username = username)

            Spacer(Modifier.height(40.dp))

            HomeMenuContent(
                onNewGame = onNewGame,
                onScoreboard = onScoreboard,
                onMarket = onMarket
            )

            Spacer(Modifier.height(32.dp))

            TextButton(
                onClick = onChangeUsername,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Adı Değiştir")
            }
        }
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
