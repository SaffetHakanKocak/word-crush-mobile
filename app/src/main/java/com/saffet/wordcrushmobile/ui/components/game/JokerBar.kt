package com.saffet.wordcrushmobile.ui.components.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saffet.wordcrushmobile.domain.model.JokerType

/**
 * Oyun ekranının altında yer alan joker barı.
 *
 * PDF §9: "Bu jokerler oyun ekranın altında seçilebilir şekilde olacaktır.
 * Marketten alındığında aktif olacak ve kullanılabilir olarak görülecektir."
 *
 * Davranış:
 *  - Her joker tipi için bir kart (görsel + isim + adet).
 *  - Adet 0 ise kart soluklaşır, yine de tıklanabilir kalır — ViewModel
 *    seviyesinde zaten "envanterde yok" mesajı döner; bu tutarlı geri
 *    bildirim sağlar.
 *  - `selectedType` ile aynı olan kart "seçili" olarak vurgulanır
 *    (targeting bekleniyorsa).
 *  - Layout yatay LazyRow — 6 joker küçük ekranda da sığar.
 */
@Composable
fun JokerBar(
    inventory: Map<JokerType, Int>,
    selectedType: JokerType?,
    onJokerClick: (JokerType) -> Unit,
    modifier: Modifier = Modifier
) {
    // Order'ı enum'daki sıra belirler — market ekranıyla birebir aynı düzen.
    val items = JokerType.entries.toList()

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(items = items, key = { it.name }) { type ->
            JokerCard(
                type = type,
                quantity = inventory[type] ?: 0,
                isSelected = selectedType == type,
                onClick = { onJokerClick(type) }
            )
        }
    }
}

/**
 * Tek bir joker kartı. Kompakt; 88.dp civarında genişliği korur.
 * Sayacın 0 olduğu durumlarda "–" gösterilir, kart sadece opaklıkla
 * farklılaşır; ViewModel zaten basmayı hatalı olarak raporlar.
 */
@Composable
private fun JokerCard(
    type: JokerType,
    quantity: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val owned = quantity > 0

    Card(
        onClick = onClick,
        modifier = Modifier.width(92.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected)
                MaterialTheme.colorScheme.onTertiaryContainer
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        border = if (isSelected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
        else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = iconFor(type),
                    contentDescription = type.displayName,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = type.displayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
            QuantityBadge(quantity = quantity, dimmed = !owned)
        }
    }
}

/**
 * Adet göstergesi. "x3" gibi kompakt gösterim, 0 ise "–".
 * dimmed=true olduğunda rengi azaltarak "yok" hissini verir.
 */
@Composable
private fun QuantityBadge(quantity: Int, dimmed: Boolean) {
    val text = if (quantity > 0) "x$quantity" else "–"
    Surface(
        shape = CircleShape,
        color = if (dimmed)
            MaterialTheme.colorScheme.surface
        else
            MaterialTheme.colorScheme.primary,
        contentColor = if (dimmed)
            MaterialTheme.colorScheme.onSurfaceVariant
        else
            MaterialTheme.colorScheme.onPrimary
    ) {
        Box(
            modifier = Modifier
                .height(20.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Joker tipine göre Material simge eşlemesi.
 * PDF'teki özel grafikler yerine yaygın Material ikonlarıyla anlamlı
 * bir görsel dil kurulur (Shuffle → karıştır, CompareArrows → swap vb.).
 * İleride PDF'teki özgün ikonlarla değiştirilmek istenirse yalnızca
 * bu map güncellenir.
 */
// Projede yalnızca `material-icons-core` var; bu yüzden seçimler
// o setle sınırlı tutulmuştur. İleride `material-icons-extended`
// eklenirse daha anlamlı ikonlarla (Shuffle, Casino, Bolt vb.)
// değiştirilebilir.
private fun iconFor(type: JokerType): ImageVector = when (type) {
    JokerType.FISH            -> Icons.Filled.Favorite
    JokerType.WHEEL           -> Icons.Filled.LocationOn
    JokerType.LOLLIPOP_HAMMER -> Icons.Filled.Clear
    JokerType.FREE_SWAP       -> Icons.AutoMirrored.Filled.ArrowForward
    JokerType.LETTER_SHUFFLE  -> Icons.Filled.Refresh
    JokerType.PARTY_BOOSTER   -> Icons.Filled.Star
}
