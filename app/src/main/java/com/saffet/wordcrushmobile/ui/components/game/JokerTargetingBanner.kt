package com.saffet.wordcrushmobile.ui.components.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saffet.wordcrushmobile.domain.model.JokerType
import com.saffet.wordcrushmobile.viewmodel.JokerTargetingState

/**
 * Bir joker seçildiğinde tahtanın hemen üstünde görünen prompt şeridi.
 *
 * "Hangi hedefi seç?" talimatını ve bir "İptal" aksiyonu içerir. Hedef
 * sayısı 1'den fazlaysa (FREE_SWAP) ilerleme ("1 / 2") gösterilir.
 *
 * [JokerTargetingState] `null` olduğunda çağıran taraf bu bileşeni
 * render etmez — burada `null-check` yapılmaz.
 */
@Composable
fun JokerTargetingBanner(
    state: JokerTargetingState,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = titleFor(state.type),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = progressText(state),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            TextButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "İptal",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("İptal")
            }
        }
    }
}

private fun titleFor(type: JokerType): String = when (type) {
    JokerType.WHEEL           -> "${type.displayName}: hedef hücreyi seç"
    JokerType.LOLLIPOP_HAMMER -> "${type.displayName}: silinecek hücreyi seç"
    JokerType.FREE_SWAP       -> "${type.displayName}: komşu 2 hücre seç"
    // Targeting moduna hedefsiz jokerler girmez; yine de savunmacı davran:
    JokerType.FISH,
    JokerType.LETTER_SHUFFLE,
    JokerType.PARTY_BOOSTER   -> type.displayName
}

private fun progressText(state: JokerTargetingState): String =
    "Seçilen ${state.pickedTargets.size} / ${state.neededTargets}"
