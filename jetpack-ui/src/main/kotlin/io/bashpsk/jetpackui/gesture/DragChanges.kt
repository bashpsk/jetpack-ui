package io.bashpsk.jetpackui.gesture

import androidx.compose.ui.geometry.Offset

sealed interface DragChanges {

    data class DragStart(val position: Offset) : DragChanges

    data object DragEnded : DragChanges

    data object DragCanceled : DragChanges

    data class HorizontalTopChanges(val changes: Float) : DragChanges

    data class HorizontalBottomChanges(val changes: Float) : DragChanges

    data class VerticalLeftChanges(val changes: ValueChange) : DragChanges

    data class VerticalRightChanges(val changes: ValueChange) : DragChanges

    data class TransformChanges(val zoom: Float, val pan: Offset) : DragChanges

    data object Unknown : DragChanges
}