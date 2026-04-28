package com.saffet.wordcrushmobile.ui.components.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.saffet.wordcrushmobile.R
import com.saffet.wordcrushmobile.domain.model.JokerType

/**
 * PDF şartnamesindeki resmi joker simgelerini gösteren reusable composable.
 *
 * Her JokerType için `res/drawable/` altındaki özel PNG kullanılır:
 *  - FISH            → joker_fish.png
 *  - WHEEL           → joker_wheel.png
 *  - LOLLIPOP_HAMMER → joker_lollipop.png
 *  - FREE_SWAP       → joker_swap.png
 *  - LETTER_SHUFFLE  → joker_shuffle.png
 *  - PARTY_BOOSTER   → joker_party.png
 *
 * @param type  Gösterilecek joker türü.
 * @param size  İkon boyutu (dp).
 */
@Composable
fun JokerIcon(
    type: JokerType,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Image(
        painter = painterResource(id = drawableFor(type)),
        contentDescription = type.displayName,
        modifier = modifier.size(size)
    )
}

/**
 * JokerType → drawable resource eşlemesi.
 */
private fun drawableFor(type: JokerType): Int = when (type) {
    JokerType.FISH            -> R.drawable.joker_fish
    JokerType.WHEEL           -> R.drawable.joker_wheel
    JokerType.LOLLIPOP_HAMMER -> R.drawable.joker_lollipop
    JokerType.FREE_SWAP       -> R.drawable.joker_swap
    JokerType.LETTER_SHUFFLE  -> R.drawable.joker_shuffle
    JokerType.PARTY_BOOSTER   -> R.drawable.joker_party
}
