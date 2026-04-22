package com.saffet.wordcrushmobile.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * HomeScreen'in sol üst köşesinde yer alan, kullanıcı adını gösteren
 * ve tıklandığında ad değiştirme dialog'unu açan küçük bileşen.
 *
 * Material 3 [AssistChip] kullanılmıştır: kalıcı bir aksiyon göstergesi
 * (küçük kalem ikonu) ile "tıklanabilir" olduğu görsel olarak belli olur.
 *
 * Boş username gösterilmez — [usernameOrNull] `null`/boş ise çağıran
 * taraf bu bileşeni hiç render etmemelidir (splash henüz yüklemediyse
 * titreme oluşmasın).
 */
@Composable
fun UsernameChip(
    username: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onClick,
        modifier = modifier,
        label = {
            Text(
                text = username,
                style = MaterialTheme.typography.labelLarge
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Kullanıcı adını değiştir"
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
