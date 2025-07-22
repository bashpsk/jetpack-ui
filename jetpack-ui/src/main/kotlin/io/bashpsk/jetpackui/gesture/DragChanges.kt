package io.bashpsk.jetpackui.gesture

import androidx.compose.ui.geometry.Offset

sealed interface DragChanges {

    data object Unknown : DragChanges

    data class ZoomChanges(val changes: Float) : DragChanges

    data class PanChanges(val changes: Offset) : DragChanges

    data class BrightnessChanges(val changes: ValueChange) : DragChanges

    data class VolumeChanges(val changes: ValueChange) : DragChanges

    data class SeekChanges(val changes: Float) : DragChanges

    data class DragStart(val position: Offset) : DragChanges

    data object DragEnded : DragChanges

    data object DragCanceled : DragChanges
}