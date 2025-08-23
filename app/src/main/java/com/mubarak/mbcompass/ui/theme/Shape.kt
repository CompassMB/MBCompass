// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

val spacingSmall = 8.dp
val spacingMedium = 16.dp


val iconDefaultSize = 24.dp

object MBShapeDefaults {

    val singleListItemShape = RoundedCornerShape(12.dp)

    val topListItemShape: RoundedCornerShape
        @Composable get() =
            RoundedCornerShape(
                topStart = shapes.large.topStart,
                topEnd = shapes.large.topEnd,
                bottomStart = shapes.extraSmall.bottomStart,
                bottomEnd = shapes.extraSmall.bottomStart
            )

    val middleListItemShape: RoundedCornerShape
        @Composable get() = RoundedCornerShape(
            topStart = shapes.extraSmall.topStart,
            topEnd = shapes.extraSmall.topEnd,
            bottomStart = shapes.extraSmall.bottomStart,
            bottomEnd = shapes.extraSmall.bottomEnd
        )

    val bottomListItemShape: RoundedCornerShape
        @Composable get() =
            RoundedCornerShape(
                topStart = shapes.extraSmall.topStart,
                topEnd = shapes.extraSmall.topEnd,
                bottomStart = shapes.large.bottomStart,
                bottomEnd = shapes.large.bottomEnd
            )
}