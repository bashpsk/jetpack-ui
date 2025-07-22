package io.bashpsk.jetpackui.gesture

import androidx.compose.ui.geometry.Offset

sealed interface TapChanges {

    data object Unknown : TapChanges

    data class SingleTap(val position: Offset) : TapChanges

    data class ForwardTap(val position: Offset) : TapChanges

    data class BackwardTap(val position: Offset) : TapChanges
}