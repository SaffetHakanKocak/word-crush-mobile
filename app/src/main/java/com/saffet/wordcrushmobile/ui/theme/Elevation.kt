package com.saffet.wordcrushmobile.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppElevations(
    val none: Dp = 0.dp,
    val low: Dp = 2.dp,
    val medium: Dp = 6.dp,
    val high: Dp = 12.dp
)

val Elevations = AppElevations()
