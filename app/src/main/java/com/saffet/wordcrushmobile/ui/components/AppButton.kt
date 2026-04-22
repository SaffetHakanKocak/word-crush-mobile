package com.saffet.wordcrushmobile.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Uygulama genelinde tekrar kullanılacak özel buton bileşeni (placeholder).
 * Ortak stil ve davranış bu tür bileşenlerde toplanır.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(onClick = onClick, modifier = modifier) {
        Text(text = text)
    }
}
