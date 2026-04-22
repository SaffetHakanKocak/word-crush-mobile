package com.saffet.wordcrushmobile.ui.components.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Kelime onaylama ve seçimi temizleme butonlarını içeren yatay satır.
 *
 * Butonlar seçim boşsa ya da sözlük hazır değilse devre dışı bırakılır —
 * bu sayede kullanıcı invalid state'te submit edemez.
 */
@Composable
fun GameActionButtons(
    canSubmit: Boolean,
    canClear: Boolean,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onClear,
            enabled = canClear,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
        ) {
            Text("Temizle")
        }
        Button(
            onClick = onSubmit,
            enabled = canSubmit,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
        ) {
            Text("Onayla")
        }
    }
}
