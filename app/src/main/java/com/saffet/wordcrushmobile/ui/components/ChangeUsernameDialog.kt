package com.saffet.wordcrushmobile.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import com.saffet.wordcrushmobile.viewmodel.HomeViewModel

/**
 * Ana ekrandaki kullanıcı adını düzenlemek için Material 3 [AlertDialog].
 *
 * State yalnızca dialog ömrü boyunca burada tutulur (`remember`):
 *  - Dialog her açıldığında mevcut [initialValue] ile sıfırlanır.
 *  - Kaydet basıldığında `onConfirm(yeni)` çağrılır; asıl persist işini
 *    [HomeViewModel.saveUsername] yapar, dialog sadece UI parçasıdır.
 *
 * Minimum uzunluk kontrolü [HomeViewModel.isUsernameValid] üzerinden
 * yapılır — UsernameScreen ile aynı kurala bağlıdır.
 */
@Composable
fun ChangeUsernameDialog(
    initialValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var input by remember(initialValue) { mutableStateOf(initialValue) }
    val currentOnConfirm by rememberUpdatedState(onConfirm)

    val isValid = HomeViewModel.isUsernameValid(input)
    val submit: () -> Unit = {
        if (isValid) {
            currentOnConfirm(input.trim())
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text("Kullanıcı Adı") },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Yeni ad") },
                supportingText = {
                    Text("En az ${HomeViewModel.MIN_USERNAME_LENGTH} karakter")
                },
                singleLine = true,
                isError = input.isNotEmpty() && !isValid,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                )
            )
        },
        confirmButton = {
            TextButton(onClick = submit, enabled = isValid) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç")
            }
        }
    )
}
