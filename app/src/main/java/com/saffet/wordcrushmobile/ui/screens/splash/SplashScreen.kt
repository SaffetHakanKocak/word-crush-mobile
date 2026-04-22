package com.saffet.wordcrushmobile.ui.screens.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saffet.wordcrushmobile.viewmodel.SplashViewModel

/**
 * Uygulama açılış ekranı.
 *
 * SplashViewModel DataStore'dan kullanıcı adını okur ve bir sonraki ekranın
 * route'unu belirler. Burada hedef null değilse [onNavigate] tetiklenerek
 * AppNavHost ilgili ekrana yönlendirir.
 */
@Composable
fun SplashScreen(
    onNavigate: (route: String) -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val nextRoute by viewModel.nextRoute.collectAsStateWithLifecycle()

    LaunchedEffect(nextRoute) {
        nextRoute?.let(onNavigate)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Word Crush", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(24.dp))
        CircularProgressIndicator()
    }
}
