package com.saffet.wordcrushmobile.ui.screens.username

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saffet.wordcrushmobile.ui.components.AppSectionCard
import com.saffet.wordcrushmobile.ui.components.AppOutlinedField
import com.saffet.wordcrushmobile.ui.components.AppPrimaryButton
import com.saffet.wordcrushmobile.ui.components.ScreenContainer
import com.saffet.wordcrushmobile.viewmodel.UsernameViewModel

/**
 * Kullanıcı adı giriş/değiştirme ekranı.
 *
 * Kayıt başarılı olduğunda [onSaved] tetiklenir; AppNavHost bu callback'i
 * Home'a yönlendirme ile bağlar. Aynı ekran hem ilk kurulumda hem de Home'dan
 * "Adı Değiştir" akışıyla kullanılır — davranış her iki durumda da aynıdır.
 */
@Composable
fun UsernameScreen(
    onSaved: () -> Unit,
    viewModel: UsernameViewModel = viewModel()
) {
    val input by viewModel.input.collectAsStateWithLifecycle()
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }

    val showValidationError = (!viewModel.isValid) &&
        (attemptedSubmit || input.isNotBlank())
    val validationMessage =
        if (input.isBlank()) "Lütfen kullanıcı adınızı girin"
        else "Kullanıcı adı en az ${UsernameViewModel.MIN_USERNAME_LENGTH} karakter olmalı"

    ScreenContainer {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            AppSectionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .animateContentSize()
            ) {
                Text(
                    text = "Profil Oluştur",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Kullanıcı adınızı girin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                AppOutlinedField(
                    value = input,
                    onValueChange = {
                        attemptedSubmit = false
                        viewModel.onInputChange(it)
                    },
                    label = "Kullanıcı adı",
                    supportingText = if (showValidationError) validationMessage else "Bu isim skor tablosunda görünecek",
                    isError = showValidationError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    )
                )

                AnimatedVisibility(
                    visible = showValidationError,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Spacer(Modifier.height(2.dp))
                        androidx.compose.foundation.layout.Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = validationMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                AppPrimaryButton(
                    text = "Devam Et",
                    onClick = {
                        attemptedSubmit = true
                        viewModel.save(onSaved)
                    },
                    enabled = viewModel.isValid,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Crossfade(targetState = viewModel.isValid, label = "usernameHint") { isValid ->
                    Text(
                        text = if (isValid) "Harika, oyuna hazırsın." else "En az ${UsernameViewModel.MIN_USERNAME_LENGTH} karakter girin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isValid) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
