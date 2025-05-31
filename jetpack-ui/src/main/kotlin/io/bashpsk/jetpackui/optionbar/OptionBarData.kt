package io.bashpsk.jetpackui.optionbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
data class OptionBarData(
    val label: String = "Label",
    val icon: ImageVector = Icons.Filled.Info
)