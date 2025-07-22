package io.bashpsk.jetpackui.gesture

import androidx.compose.runtime.Immutable

@Immutable
data class VideoGestureConfig(
    val isDoubleTapEnable: Boolean = true,
    val isHorizontalSeekEnable: Boolean = true,
    val isBrightnessEnable: Boolean = true,
    val isVolumeEnable: Boolean = true,
    val isZoomEnable: Boolean = true,
    val isPanEnable: Boolean = true,
    val minimumLengthSeek: Int = 25,
    val minimumLengthBrightness: Int = 25,
    val minimumLengthVolume: Int = 25,
)