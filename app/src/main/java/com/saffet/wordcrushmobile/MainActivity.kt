package com.saffet.wordcrushmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.saffet.wordcrushmobile.ui.navigation.AppNavHost
import com.saffet.wordcrushmobile.ui.theme.WordCrushMobileTheme

/**
 * Uygulamanın tek Activity'si.
 * Tema ve Scaffold'u kurar, ardından ekranlar arası geçişi AppNavHost'a devreder.
 *
 * Not: Composable wrapper'a [WordCrushRoot] adı verildi; aynı paketteki
 * [WordCrushApp] Application sınıfı ile ad çakışmasını önlemek için gereklidir.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordCrushRoot()
        }
    }
}

@Composable
private fun WordCrushRoot() {
    WordCrushMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AppNavHost()
            }
        }
    }
}
